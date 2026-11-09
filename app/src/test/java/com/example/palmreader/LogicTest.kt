package com.example.palmreader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class LogicTest {

    @Test
    fun sunSignBoundaries() {
        assertEquals(Sign.CAPRICORN, Sign.of(LocalDate.of(2000, 1, 19)))
        assertEquals(Sign.AQUARIUS, Sign.of(LocalDate.of(2000, 1, 20)))
        assertEquals(Sign.PISCES, Sign.of(LocalDate.of(2000, 3, 20)))
        assertEquals(Sign.ARIES, Sign.of(LocalDate.of(2000, 3, 21)))
        assertEquals(Sign.LEO, Sign.of(LocalDate.of(1995, 8, 1)))
        assertEquals(Sign.CAPRICORN, Sign.of(LocalDate.of(2000, 12, 31)))
    }

    @Test
    fun loveScoreIsStableSymmetricAndInRange() {
        val s = loveScore("Nguyễn Văn An", "Trần Thị Bình")
        assertEquals(s, loveScore("tran thi binh", "NGUYEN VAN AN"))
        for (a in listOf("An", "Bình", "Chi", "Dũng", "Em")) for (b in listOf("Hoa", "Lan", "Minh")) {
            val score = loveScore(a, b)
            assertTrue(score in 0..100)
            loveBand(score) // không ném lỗi = mọi điểm đều có band
        }
    }

    @Test
    fun dailyReportIsDeterministic() {
        val day = LocalDate.of(2026, 11, 9)
        assertEquals(dailyReport(Sign.LEO, day), dailyReport(Sign.LEO, day))
        val d = dailyReport(Sign.LEO, day)
        assertEquals(3, d.numbers.toSet().size)
        assertTrue(d.strength in 60..99)
    }

    @Test
    fun tarotDeckHas78UniqueCards() {
        assertEquals(78, TAROT_DECK.size)
        assertEquals(78, TAROT_DECK.map { it.name }.toSet().size)
    }

    @Test
    fun ascendantAtEquatorWithAriesOnMidheavenIsCancer() {
        assertEquals(90.0, ascendant(0.0, 0.0, 23.44), 1e-9)
        assertEquals(180.0, ascendant(90.0, 0.0, 23.44), 1e-9)
    }

    @Test
    fun sunEntersSiderealAriesAtMeshaSankranti2024() {
        // Mesha Sankranti 2024 (Lahiri): 13/04/2024 khoảng 21:15 IST = 15:45 UTC
        val sun = vedicChart(Instant.parse("2024-04-13T15:45:00Z"), 21.0, 105.8).of(Graha.SUN).longitude
        val diff = ((sun + 180) % 360) - 180
        assertTrue("sun=$sun", kotlin.math.abs(diff) < 0.3)
    }

    @Test
    fun ketuOppositeRahuAndDashaAdvancesPastBirth() {
        val birth = Instant.parse("1990-05-15T03:30:00Z")
        val chart = vedicChart(birth, 21.0, 105.8, now = Instant.parse("2026-11-09T00:00:00Z"))
        val diff = (chart.of(Graha.KETU).longitude - chart.of(Graha.RAHU).longitude + 360) % 360
        assertEquals(180.0, diff, 1e-9)
        assertTrue(chart.dashaEnds.isAfter(Instant.parse("2026-11-09T00:00:00Z")))
        // nakshatra Ashwini (0), vừa vào: thời kỳ đầu là Ketu 7 năm
        val (lord, end) = currentDasha(birth, 0, 0.0, birth)
        assertEquals(Graha.KETU, lord)
        assertEquals(birth.plusSeconds((7 * 365.25 * 86400).toLong()), end)
    }
}
