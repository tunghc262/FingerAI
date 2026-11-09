package com.example.palmreader

import android.app.Activity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Hai API công khai mà app gốc dùng (không phải server riêng của nhà phát triển). */
object Net {
    private fun getJson(url: String): JSONObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        try {
            if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
            return JSONObject(conn.inputStream.bufferedReader().readText())
        } finally {
            conn.disconnect()
        }
    }

    fun dailyHoroscope(sign: Sign): String =
        getJson("https://freehoroscopeapi.com/api/v1/get-horoscope/daily?sign=${sign.api}")
            .getJSONObject("data").getString("horoscope")

    data class Place(val label: String, val lat: Double, val lon: Double, val zone: String)

    fun searchPlace(query: String): List<Place> {
        val q = URLEncoder.encode(query, "UTF-8")
        val results = getJson("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=10&language=vi&format=json")
            .optJSONArray("results") ?: return emptyList()
        return (0 until results.length()).map { i ->
            val o = results.getJSONObject(i)
            val label = listOf(o.optString("name"), o.optString("admin1"), o.optString("country")).filter { it.isNotBlank() }.distinct().joinToString(", ")
            Place(label, o.getDouble("latitude"), o.getDouble("longitude"), o.optString("timezone", "UTC"))
        }
    }
}

/** Chạy [work] ở luồng nền rồi trả kết quả (hoặc lỗi) về luồng UI. */
fun <T> Activity.background(work: () -> T, done: (Result<T>) -> Unit) {
    Thread {
        val r = runCatching(work)
        runOnUiThread { if (!isFinishing && !isDestroyed) done(r) }
    }.start()
}
