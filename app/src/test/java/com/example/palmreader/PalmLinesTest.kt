package com.example.palmreader

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** So bản port Kotlin với pipeline Python tham chiếu (fixtures sinh từ yeonsumia/palmistry + mask U-Net). */
class PalmLinesTest {
    private val samples = listOf("hand1", "hand48", "hand6", "hand70")

    private fun mask(name: String): BooleanArray {
        val on = javaClass.getResourceAsStream("/palm/${name}_mask.txt")!!.bufferedReader().readText().split(' ').map { it.toInt() }
        return BooleanArray(PalmLines.SIZE * PalmLines.SIZE).also { m -> on.forEach { m[it] = true } }
    }

    /** Đọc mảng số lồng nhau trong JSON (đủ cho fixtures, không cần thư viện). */
    private fun parse(s: String, start: Int): Pair<Any?, Int> {
        var i = start
        while (s[i] == ' ') i++
        if (s.startsWith("null", i)) return null to i + 4
        if (s[i] != '[') {
            val end = s.indexOfAny(charArrayOf(',', ']'), i)
            return s.substring(i, end).trim().toDouble() to end
        }
        val out = ArrayList<Any?>()
        i++
        while (true) {
            while (s[i] == ' ' || s[i] == ',') i++
            if (s[i] == ']') return out to i + 1
            val (v, j) = parse(s, i)
            out += v
            i = j
        }
    }

    private fun field(name: String, key: String): List<*> {
        val s = javaClass.getResourceAsStream("/palm/$name.json")!!.bufferedReader().readText()
        return parse(s, s.indexOf("\"$key\":") + key.length + 3).first as List<*>
    }

    @Test
    fun kotlinPortMatchesPythonReference() {
        for (name in samples) {
            val s = PalmLines.SIZE
            val lines = PalmLines.classify(PalmLines.group(PalmLines.skeletonize(mask(name), s, s), s, s))
            val expected = field(name, "lines")
            assertEquals(name, expected.size, lines.size)
            for (k in 0 until 3) {
                val exp = (expected[k] as List<*>).map { p -> (p as List<*>).map { (it as Double).toInt() }.toIntArray() }
                val got = lines[k]!!
                assertEquals("$name line $k length", exp.size, got.size)
                exp.zip(got).forEach { (e, g) -> assertArrayEquals("$name line $k", e, g) }
            }
        }
    }

    @Test
    fun measureGivesAnAnswerForEverySample() {
        for (name in samples) {
            val s = PalmLines.SIZE
            val lines = PalmLines.classify(PalmLines.group(PalmLines.skeletonize(mask(name), s, s), s, s))
            // fixture lưu điểm mốc phát hiện trên ảnh đã lật ngang -> lật lại
            val lm = field(name, "warped_landmarks").map { p -> (p as List<*>).let { 1 - (it[0] as Double) to it[1] as Double } }
            assertNotNull(name, PalmLines.measure(lines, lm))
        }
    }

    @Test
    fun homographyRecoversKnownTransform() {
        val h = doubleArrayOf(1.2, 0.1, 5.0, -0.05, 0.9, 3.0, 0.0004, -0.0002, 1.0)
        val src = PalmLines.TARGET.map { (x, y) -> x * 300 to y * 400 }
        val dst = src.map { (x, y) -> PalmLines.project(h, x, y) }
        val got = PalmLines.homography(src, dst)
        for (i in 0 until 9) assertEquals(h[i], got[i], 1e-6)
    }

    @Test
    fun backgroundRemovalKeepsSkinAndWhitensRest() {
        val skin = 0xFFE0AC90.toInt()
        val blue = 0xFF2040C0.toInt()
        val dark = 0xFF302020.toInt()
        val out = PalmLines.removeBackground(intArrayOf(skin, blue, dark))
        assertEquals(skin, out[0])
        assertEquals(-1, out[1])
        assertEquals(-1, out[2])
    }

    @Test
    fun classifyNeverPicksSameLineTwice() {
        val line = List(20) { intArrayOf(100, 50 + it, 0, 1) }
        val picked = PalmLines.classify(listOf(line, line.map { intArrayOf(it[0] + 30, it[1], 0, 1) }))
        assertTrue(picked[0] !== picked[1])
        assertEquals(null, picked[2])
    }
}
