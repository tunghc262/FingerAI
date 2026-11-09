package com.example.palmreader

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.palmreader.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Profile.load(this).birth == null) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnProfile.setOnClickListener { open(ProfileActivity::class.java) }
        b.btnScan.setOnClickListener { open(PalmGuideActivity::class.java) }
        b.tileHoroscope.setOnClickListener { open(HoroscopeActivity::class.java) }
        b.tileTarot.setOnClickListener { open(TarotActivity::class.java) }
        b.tileLove.setOnClickListener { open(LoveActivity::class.java) }
        b.tileAstrology.setOnClickListener { open(AstrologyActivity::class.java) }
    }

    override fun onResume() {
        super.onResume()
        if (!::b.isInitialized) return
        val p = Profile.load(this)
        val greeting = when (LocalTime.now().hour) {
            in 5..11 -> "Chào buổi sáng"
            in 12..17 -> "Chào buổi chiều"
            else -> "Chào buổi tối"
        }
        b.tvGreeting.text = if (p.name.isBlank()) greeting else "$greeting, ${p.name}"
        val sign = p.sign
        b.tvSign.text = sign?.let { "${it.label} · Khám phá vận mệnh của bạn" } ?: "Khám phá vận mệnh của bạn"
        if (sign == null) {
            b.cardDaily.visibility = View.GONE
            return
        }
        val d = dailyReport(sign, LocalDate.now())
        b.tvDaily.text = "Số may mắn: ${d.numbers.joinToString(", ")}\n" +
            "Màu may mắn: ${d.colors.joinToString(", ")}\n" +
            "Năng lượng: ${d.strength}%\n" +
            "Lời khuyên: ${d.tip}"
        b.cardDaily.visibility = View.VISIBLE
    }

    private fun open(c: Class<*>) = startActivity(Intent(this, c))
}
