package com.example.palmreader

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.palmreader.databinding.ActivityHoroscopeBinding

class HoroscopeActivity : AppCompatActivity() {
    private lateinit var b: ActivityHoroscopeBinding
    private val signs = Sign.entries

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHoroscopeBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.spSign.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, signs.map { "${it.label} (${it.start.dayOfMonth}/${it.start.monthValue})" })
        Profile.load(this).sign?.let { b.spSign.setSelection(it.ordinal) }
        b.spSign.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) = show(signs[pos])
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        b.btnRetry.setOnClickListener { show(signs[b.spSign.selectedItemPosition]) }
    }

    private fun show(sign: Sign) {
        b.tvSoul.text = "Nguyên tố: ${sign.element} · Hành tinh chủ quản: ${sign.ruler}\n\n" +
            sign.sections().joinToString("\n\n") { (title, text) -> "$title\n$text" }

        b.btnRetry.visibility = View.GONE
        b.tvHoroscope.text = "Đang tải…"
        background({ Net.dailyHoroscope(sign) }) { r ->
            if (signs[b.spSign.selectedItemPosition] != sign) return@background // người dùng đã đổi cung
            r.onSuccess { b.tvHoroscope.text = it }
            r.onFailure {
                b.tvHoroscope.text = "Không tải được tử vi hôm nay (${it.message}). Kiểm tra kết nối mạng."
                b.btnRetry.visibility = View.VISIBLE
            }
        }
    }
}
