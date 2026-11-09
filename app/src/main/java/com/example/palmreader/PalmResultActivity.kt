package com.example.palmreader

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.inSpans
import com.example.palmreader.databinding.ActivityPalmResultBinding

class PalmResultActivity : AppCompatActivity() {

    /** Ý nghĩa diễn giải từ measurement.py của repo gốc, viết lại bằng tiếng Việt. */
    private class LineInfo(val name: String, val color: Int, val about: String, val long: String, val short: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityPalmResultBinding.inflate(layoutInflater)
        setContentView(b.root)
        val r = PalmScanner.last ?: return finish() // tiến trình bị hệ thống thu hồi -> không còn kết quả

        val infos = listOf(
            LineInfo("Đường tâm đạo", ContextCompat.getColor(this, R.color.line_heart),
                "Chi phối cảm xúc, tình yêu, tình bạn và sự gắn bó.",
                "Đường tâm đạo dài: bạn yêu sâu đậm và bền bỉ, có xu hướng gắn bó lâu dài với người mình thương.",
                "Đường tâm đạo ngắn: bạn cởi mở, dễ kết nối với nhiều kiểu người và sẽ trải qua nhiều mối quan hệ phong phú."),
            LineInfo("Đường trí đạo", ContextCompat.getColor(this, R.color.line_head),
                "Phản ánh cách bạn suy nghĩ, học hỏi và theo đuổi tri thức.",
                "Đường trí đạo dài: bạn tò mò về nhiều lĩnh vực và thích khám phá kiến thức thật rộng.",
                "Đường trí đạo ngắn: bạn thích tập trung vào một chủ đề và đào sâu đến tận cùng."),
            LineInfo("Đường sinh đạo", ContextCompat.getColor(this, R.color.line_life),
                "Nói về sức sống, năng lượng và trải nghiệm – không liên quan đến tuổi thọ.",
                "Đường sinh đạo dài: bạn thường giải quyết vấn đề cùng mọi người và dựa vào sự gắn kết.",
                "Đường sinh đạo ngắn: bạn độc lập, tự chủ và quen tự mình tìm lối đi."),
        )
        val longs = listOf(r.lengths.heartLong, r.lengths.headLong, r.lengths.lifeLong)

        b.ivPalm.setImageBitmap(draw(r, infos.map { it.color }))
        b.tvLegend.text = SpannableStringBuilder().apply {
            infos.forEachIndexed { i, info ->
                if (i > 0) append("   ")
                inSpans(ForegroundColorSpan(info.color)) { append("━ ") }
                append(info.name)
            }
        }
        listOf(b.tvHeart, b.tvHead, b.tvLife).forEachIndexed { i, tv ->
            val info = infos[i]
            tv.text = SpannableStringBuilder().apply {
                inSpans(ForegroundColorSpan(info.color), StyleSpan(Typeface.BOLD), RelativeSizeSpan(1.15f)) {
                    append(info.name).append(if (longs[i]) " · Dài" else " · Ngắn")
                }
                append("\n").append(info.about).append("\n\n").append(if (longs[i]) info.long else info.short)
            }
        }
        b.btnAgain.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
        }
    }

    /** Phóng ảnh đã nắn lên 3 lần rồi vẽ 3 đường (tọa độ [y, x] trong ảnh 256x256). */
    private fun draw(r: PalmScanner.Result, colors: List<Int>): Bitmap {
        val k = 3f
        val out = Bitmap.createScaledBitmap(r.image, (r.image.width * k).toInt(), (r.image.height * k).toInt(), true).copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 7f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        r.lines.forEachIndexed { i, line ->
            val path = Path()
            line.forEachIndexed { j, p ->
                val x = (p[1] + 0.5f) * k
                val y = (p[0] + 0.5f) * k
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, paint.apply { color = colors[i] })
        }
        return out
    }
}
