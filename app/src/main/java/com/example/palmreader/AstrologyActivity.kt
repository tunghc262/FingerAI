package com.example.palmreader

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.palmreader.databinding.ActivityAstrologyBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AstrologyActivity : AppCompatActivity() {
    private lateinit var b: ActivityAstrologyBinding
    private val key get() = if (b.toggle.checkedButtonId == b.btnOther.id) Profile.KEY_OTHER else Profile.KEY_ME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAstrologyBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toggle.addOnButtonCheckedListener { _, _, checked -> if (checked) render() }
        b.btnEdit.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).putExtra(ProfileActivity.EXTRA_KEY, key))
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val p = Profile.load(this, key)
        b.table.removeAllViews()
        if (p.birth == null || !p.hasPlace) {
            b.tvWho.text = "Cần tên, ngày giờ sinh và nơi sinh để lập lá số."
            b.tvSummary.text = ""
            b.btnEdit.text = "Nhập thông tin sinh"
            return
        }
        b.btnEdit.text = "Sửa thông tin sinh"
        b.tvWho.text = "${p.name} · ${p.birth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))} · ${p.place}"

        val chart = vedicChart(p.birthInstant(), p.lat, p.lon)
        row(listOf("Graha", "Rasi", "Độ"), header = true)
        chart.positions.forEach { pos ->
            val d = pos.degreeInSign
            row(listOf(pos.graha.vi, pos.sign.label, "%d°%02d'".format(d.toInt(), ((d % 1) * 60).toInt())))
        }

        val moonSign = chart.of(Graha.MOON).sign
        val dashaEnd = chart.dashaEnds.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        b.tvSummary.text = """
            Cung mọc (Lagna): ${chart.of(Graha.LAGNA).sign.label}
            Nakshatra Mặt Trăng: ${chart.nakshatra}
            Đại vận hiện tại (Vimshottari): ${chart.dashaLord.vi}, đến $dashaEnd

            ${moonSign.moon}
        """.trimIndent()
    }

    private fun row(cells: List<String>, header: Boolean = false) {
        val tr = TableRow(this)
        cells.forEach { c ->
            tr.addView(TextView(this).apply {
                text = c
                setPadding(8, 12, 8, 12)
                if (header) setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
        }
        b.table.addView(tr)
    }
}
