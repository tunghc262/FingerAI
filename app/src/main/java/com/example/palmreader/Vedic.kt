package com.example.palmreader

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.geoMoon
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.siderealTime
import java.time.Duration
import java.time.Instant
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class Graha(val vi: String, val dashaYears: Int) {
    LAGNA("Lagna (cung mọc)", 0),
    SUN("Mặt Trời", 6),
    MOON("Mặt Trăng", 10),
    MARS("Sao Hỏa", 7),
    MERCURY("Sao Thủy", 17),
    JUPITER("Sao Mộc", 16),
    VENUS("Sao Kim", 20),
    SATURN("Sao Thổ", 19),
    RAHU("Rahu", 18),
    KETU("Ketu", 7),
}

data class GrahaPosition(val graha: Graha, val longitude: Double) {
    val sign get() = Sign.entries[(longitude / 30).toInt() % 12]
    val degreeInSign get() = longitude % 30
}

data class VedicChart(
    val positions: List<GrahaPosition>,
    val nakshatra: String,
    val dashaLord: Graha,
    val dashaEnds: Instant,
) {
    fun of(g: Graha) = positions.first { it.graha == g }
}

val NAKSHATRAS = listOf(
    "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu", "Pushya", "Ashlesha",
    "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
    "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati",
)

/** Thứ tự các thời kỳ Vimshottari, bắt đầu từ chủ tinh của Ashwini. */
private val DASHA_ORDER = listOf(Graha.KETU, Graha.VENUS, Graha.SUN, Graha.MOON, Graha.MARS, Graha.RAHU, Graha.JUPITER, Graha.SATURN, Graha.MERCURY)

private const val LAHIRI_J2000 = 23.853 // ayanamsa Lahiri tại J2000
private const val NAK_SPAN = 360.0 / 27
private const val YEAR_DAYS = 365.25

private fun norm(x: Double) = ((x % 360) + 360) % 360
private fun rad(d: Double) = Math.toRadians(d)

/** Kinh độ cung mọc (độ, hoàng đạo của ngày) từ RAMC, vĩ độ và độ nghiêng hoàng đạo. */
fun ascendant(ramcDeg: Double, latDeg: Double, oblDeg: Double): Double =
    norm(Math.toDegrees(atan2(cos(rad(ramcDeg)), -(sin(rad(ramcDeg)) * cos(rad(oblDeg)) + tan(rad(latDeg)) * sin(rad(oblDeg))))))

/**
 * Lá số Vệ Đà (sidereal, Lahiri) tại thời điểm sinh.
 * equatorialToEcliptic trả về hoàng đạo thật của ngày, nên mọi kinh độ đều trừ ayanamsa của ngày.
 */
fun vedicChart(birth: Instant, latitude: Double, longitude: Double, now: Instant = Instant.now()): VedicChart {
    val t = Time.fromMillisecondsSince1970(birth.toEpochMilli())
    val centuries = t.tt / 36525.0
    val ayanamsaOfDate = LAHIRI_J2000 + centuries * 100 * 50.29 / 3600

    fun sid(b: Body) = norm(equatorialToEcliptic(geoVector(b, t, Aberration.Corrected)).elon - ayanamsaOfDate)
    val moon = norm(equatorialToEcliptic(geoMoon(t)).elon - ayanamsaOfDate)

    val obliquity = 23.4392911 - 0.0130042 * centuries
    val ramc = norm(siderealTime(t) * 15 + longitude)
    val lagna = norm(ascendant(ramc, latitude, obliquity) - ayanamsaOfDate)
    val rahu = norm(125.0445479 - 1934.1362891 * centuries - ayanamsaOfDate) // nút Bắc trung bình

    val positions = listOf(
        GrahaPosition(Graha.LAGNA, lagna),
        GrahaPosition(Graha.SUN, sid(Body.Sun)),
        GrahaPosition(Graha.MOON, moon),
        GrahaPosition(Graha.MARS, sid(Body.Mars)),
        GrahaPosition(Graha.MERCURY, sid(Body.Mercury)),
        GrahaPosition(Graha.JUPITER, sid(Body.Jupiter)),
        GrahaPosition(Graha.VENUS, sid(Body.Venus)),
        GrahaPosition(Graha.SATURN, sid(Body.Saturn)),
        GrahaPosition(Graha.RAHU, rahu),
        GrahaPosition(Graha.KETU, norm(rahu + 180)),
    )

    val nak = floor(moon / NAK_SPAN).toInt()
    val (lord, ends) = currentDasha(birth, nak, (moon % NAK_SPAN) / NAK_SPAN, now)
    return VedicChart(positions, NAKSHATRAS[nak], lord, ends)
}

/** Thời kỳ Vimshottari đang chạy: [elapsed] là phần nakshatra Mặt Trăng đã đi qua lúc sinh (0..1). */
fun currentDasha(birth: Instant, nakshatra: Int, elapsed: Double, now: Instant): Pair<Graha, Instant> {
    var i = nakshatra % 9
    fun years(g: Graha) = Duration.ofSeconds((g.dashaYears * YEAR_DAYS * 86400).toLong())
    var end = birth.plus(years(DASHA_ORDER[i]).multipliedBy(((1 - elapsed) * 1_000_000).toLong()).dividedBy(1_000_000))
    while (!end.isAfter(now)) {
        i = (i + 1) % 9
        end = end.plus(years(DASHA_ORDER[i]))
    }
    return DASHA_ORDER[i] to end
}
