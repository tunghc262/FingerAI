package com.example.palmreader

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Chạy toàn bộ pipeline thật (MediaPipe + ONNX) trên thiết bị với ảnh mẫu của repo palmistry. */
@RunWith(AndroidJUnit4::class)
class PalmScannerTest {
    @Test
    fun scansAllSampleHands() {
        val inst = InstrumentationRegistry.getInstrumentation()
        PalmScanner(inst.targetContext).use { scanner ->
            for (name in listOf("hand1", "hand48", "hand6", "hand70")) {
                val photo = inst.context.assets.open("$name.jpg").use { BitmapFactory.decodeStream(it) }
                val r = scanner.scan(photo)
                assertEquals(name, 3, r.lines.size)
                assertTrue(name, r.lines.all { it.size >= 10 })
            }
        }
    }
}
