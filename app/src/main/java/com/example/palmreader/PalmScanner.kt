package com.example.palmreader

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import java.io.Closeable
import java.nio.FloatBuffer

/**
 * Ghép pipeline: MediaPipe (điểm mốc) -> nắn ảnh -> U-Net ONNX -> PalmLines. Tạo ở luồng nền vì nạp model mất ~1 giây.
 * Chỉ có một HandLandmarker cho cả app: tạo hai cái song song làm MediaPipe nạp model lỗi ("DEQUANTIZE is not produced").
 */
class PalmScanner(context: Context) : Closeable {
    private val landmarker = handLandmarker(context)
    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession = env.createSession(context.assets.open("palm_unet.onnx").use { it.readBytes() })

    class Result(val image: Bitmap, val lines: List<Line>, val lengths: PalmLines.Lengths)
    class Failure(message: String) : Exception(message)

    /** 21 điểm mốc chuẩn hóa (0..1) của bàn tay đầu tiên, hoặc null. An toàn khi gọi từ nhiều luồng. */
    fun landmarks(bitmap: Bitmap): List<Pair<Double, Double>>? = synchronized(landmarker) {
        landmarker.detect(BitmapImageBuilder(bitmap).build()).landmarks().firstOrNull()
            ?.map { it.x().toDouble() to it.y().toDouble() }
    }

    fun scan(photo: Bitmap): Result {
        val lm = landmarks(photo) ?: throw Failure("Không thấy bàn tay. Hãy mở lòng bàn tay hướng vào camera, đủ ánh sáng.")
        if (lm.maxOf { it.first } - lm.minOf { it.first } < 0.25 && lm.maxOf { it.second } - lm.minOf { it.second } < 0.25)
            throw Failure("Bàn tay quá nhỏ trong ảnh. Hãy đưa camera lại gần hơn.")

        // homography trên tọa độ chuẩn hóa 0..1 cho ổn định số, rồi đổi sang pixel bằng Matrix
        val h = PalmLines.homography(lm, PalmLines.TARGET)
        val s = PalmLines.SIZE
        val warped = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
        Canvas(warped).drawBitmap(photo, Matrix().apply {
            setValues(FloatArray(9) { h[it].toFloat() })
            preScale(1f / photo.width, 1f / photo.height)
            postScale(s.toFloat(), s.toFloat())
        }, null) // không lọc = nearest, giống PIL NEAREST của repo

        val pixels = IntArray(s * s).also { warped.getPixels(it, 0, s, 0, 0, s, s) }
        val input = OnnxTensor.createTensor(env, FloatBuffer.wrap(PalmLines.toInput(PalmLines.removeBackground(pixels))), longArrayOf(1, 3, s.toLong(), s.toLong()))
        val logits = input.use { session.run(mapOf("image" to it)).use { r -> (r[0].value as Array<Array<Array<FloatArray>>>)[0][0] } }
        val flat = FloatArray(s * s) { logits[it / s][it % s] }

        val lines = PalmLines.classify(PalmLines.group(PalmLines.skeletonize(PalmLines.mask(flat), s, s), s, s))
        val lengths = PalmLines.measure(lines, lm.map { (x, y) -> PalmLines.project(h, x, y) })
            ?: throw Failure("Chưa tìm đủ 3 đường chỉ tay chính. Hãy chụp lại với lòng bàn tay mở phẳng, đủ sáng, không đeo nhẫn.")
        return Result(warped, lines.map { it!! }, lengths)
    }

    override fun close() {
        synchronized(landmarker) { landmarker.close() }
        session.close()
    }

    companion object {
        /** Kết quả lần quét gần nhất, truyền sang màn kết quả (Bitmap quá lớn cho Intent). */
        var last: Result? = null

        private fun handLandmarker(context: Context): HandLandmarker = HandLandmarker.createFromOptions(
            context,
            HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build())
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(1)
                .setMinHandDetectionConfidence(0.5f)
                .build(),
        )
    }
}
