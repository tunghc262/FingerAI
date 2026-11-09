package com.example.palmreader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.palmreader.databinding.ActivityScanBinding
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/** Camera quét tay: hướng dẫn trực tiếp bằng MediaPipe, chụp (hoặc chọn ảnh) rồi chạy PalmScanner. */
class ScanActivity : AppCompatActivity() {
    private lateinit var b: ActivityScanBinding
    private val worker = Executors.newSingleThreadExecutor() // nạp model rồi phân tích, tuần tự
    private lateinit var scanner: Future<PalmScanner> // tạo trong onCreate: trước đó Activity chưa có Context
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var lastAnalysis = 0L
    private var camera: Camera? = null
    private var capture: ImageCapture? = null
    private var torch = false

    private val askCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else hint("Cần quyền camera để quét. Bạn vẫn có thể chọn ảnh từ thư viện.")
    }
    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let { analyze { decode(it) } } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityScanBinding.inflate(layoutInflater)
        setContentView(b.root)
        scanner = worker.submit<PalmScanner> { PalmScanner(applicationContext) }

        b.btnClose.setOnClickListener { finish() }
        b.btnGallery.setOnClickListener { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        b.btnFlash.setOnClickListener { torch = !torch; camera?.cameraControl?.enableTorch(torch) }
        b.btnShutter.setOnClickListener { takePhoto() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else askCamera.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(b.preview.surfaceProvider) }
            val cap = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
            val analysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(analysisExecutor, ::guide)
            try {
                provider.unbindAll()
                camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, cap, analysis)
                capture = cap
                b.btnFlash.visibility = if (camera!!.cameraInfo.hasFlashUnit()) View.VISIBLE else View.INVISIBLE
            } catch (e: Exception) {
                hint("Không mở được camera. Bạn vẫn có thể chọn ảnh từ thư viện.")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /** Chạy trên analysisExecutor ~3 lần/giây: báo người dùng tay đã vào khung chưa. */
    private fun guide(proxy: ImageProxy) {
        val now = SystemClock.uptimeMillis()
        // chờ model nạp xong; nạp lỗi thì analyze() sẽ báo khi người dùng bấm quét
        val s = if (scanner.isDone) runCatching { scanner.get() }.getOrNull() else null
        if (s == null || now - lastAnalysis < 300) return proxy.close()
        lastAnalysis = now
        val bmp = proxy.use { it.toBitmap().rotated(it.imageInfo.rotationDegrees) }
        val points = runCatching { s.landmarks(bmp) }.getOrNull() // scanner có thể vừa bị đóng khi thoát màn
        val span = points?.let { p -> maxOf(p.maxOf { it.first } - p.minOf { it.first }, p.maxOf { it.second } - p.minOf { it.second }) }
        runOnUiThread {
            when {
                points == null -> hint("Đặt lòng bàn tay vào trong khung")
                span!! < 0.35 -> hint("Đưa tay lại gần hơn")
                else -> hint("Đã nhận diện bàn tay, chạm Quét")
            }
            b.btnShutter.isEnabled = points != null && span!! >= 0.35
        }
    }

    private fun takePhoto() {
        val cap = capture ?: return
        b.btnShutter.isEnabled = false
        cap.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bmp = image.use { it.toBitmap().rotated(it.imageInfo.rotationDegrees) }
                analyze { bmp.limited() }
            }

            override fun onError(e: ImageCaptureException) {
                b.btnShutter.isEnabled = true
                fail("Không chụp được ảnh. Hãy thử lại.")
            }
        })
    }

    private fun analyze(photo: () -> Bitmap) {
        b.overlay.visibility = View.VISIBLE
        worker.execute {
            val r = runCatching { scanner.get().scan(photo()) }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                b.overlay.visibility = View.GONE
                r.onSuccess {
                    PalmScanner.last = it
                    startActivity(Intent(this, PalmResultActivity::class.java))
                    finish()
                }
                r.onFailure { e ->
                    val cause = if (e is ExecutionException) e.cause ?: e else e
                    fail(if (cause is PalmScanner.Failure) cause.message!! else "Có lỗi khi phân tích ảnh: ${cause.message}")
                }
            }
        }
    }

    private fun decode(uri: Uri): Bitmap =
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { d, info, _ ->
            d.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // MediaPipe và Canvas cần bitmap phần mềm
            val m = maxOf(info.size.width, info.size.height)
            if (m > MAX_SIDE) d.setTargetSampleSize(m / MAX_SIDE)
        }.let { if (it.config == Bitmap.Config.ARGB_8888) it else it.copy(Bitmap.Config.ARGB_8888, false) }

    private fun hint(text: String) { b.tvHint.text = text }

    private fun fail(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Ảnh chưa đạt")
            .setMessage(message)
            .setPositiveButton("Chụp lại", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        analysisExecutor.shutdown()
        worker.execute {
            analysisExecutor.awaitTermination(2, TimeUnit.SECONDS) // khung hình cuối đang dùng landmarker
            runCatching { scanner.get().close() }
        }
        worker.shutdown()
    }

    private fun Bitmap.rotated(degrees: Int): Bitmap =
        if (degrees == 0) this else Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { postRotate(degrees.toFloat()) }, true)

    private fun Bitmap.limited(): Bitmap {
        val m = maxOf(width, height)
        return if (m <= MAX_SIDE) this else Bitmap.createScaledBitmap(this, width * MAX_SIDE / m, height * MAX_SIDE / m, true)
    }

    companion object {
        private const val MAX_SIDE = 1600 // điểm mốc là tọa độ chuẩn hóa, ảnh lớn hơn chỉ làm chậm
    }
}
