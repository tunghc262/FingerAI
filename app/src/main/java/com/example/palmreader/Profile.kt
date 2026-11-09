package com.example.palmreader

import android.content.Context
import java.time.LocalDateTime
import java.time.ZoneId

/** Hồ sơ người dùng, lưu trong SharedPreferences. [birth] là giờ địa phương tại nơi sinh. */
data class Profile(
    val name: String = "",
    val gender: String = "",
    val birth: LocalDateTime? = null,
    val place: String = "",
    val lat: Double = Double.NaN,
    val lon: Double = Double.NaN,
    val zone: String = "",
) {
    val sign get() = birth?.let { Sign.of(it.toLocalDate()) }
    val hasPlace get() = !lat.isNaN() && !lon.isNaN() && zone.isNotEmpty()
    fun birthInstant() = birth!!.atZone(ZoneId.of(zone)).toInstant()

    fun save(c: Context, key: String = KEY_ME) {
        c.getSharedPreferences(key, Context.MODE_PRIVATE).edit()
            .putString("name", name).putString("gender", gender)
            .putString("birth", birth?.toString()).putString("place", place)
            .putString("lat", lat.toString()).putString("lon", lon.toString()).putString("zone", zone)
            .apply()
    }

    companion object {
        const val KEY_ME = "profile"
        const val KEY_OTHER = "profile_other"

        fun load(c: Context, key: String = KEY_ME): Profile {
            val p = c.getSharedPreferences(key, Context.MODE_PRIVATE)
            return Profile(
                name = p.getString("name", "")!!,
                gender = p.getString("gender", "")!!,
                birth = p.getString("birth", null)?.let(LocalDateTime::parse),
                place = p.getString("place", "")!!,
                lat = p.getString("lat", null)?.toDoubleOrNull() ?: Double.NaN,
                lon = p.getString("lon", null)?.toDoubleOrNull() ?: Double.NaN,
                zone = p.getString("zone", "")!!,
            )
        }
    }
}
