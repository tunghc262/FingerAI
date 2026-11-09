package com.example.palmreader

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.palmreader.databinding.ActivityOnboardingBinding
import java.time.LocalDate
import java.time.LocalTime

/** Giới thiệu 3 trang -> tên & giới tính -> ngày sinh, giống luồng onboarding của app gốc. */
class OnboardingActivity : AppCompatActivity() {
    private lateinit var b: ActivityOnboardingBinding
    private var step = 0

    private val intro = listOf(
        Triple("🖐", "Bàn tay kể chuyện của bạn", "Quét lòng bàn tay để AI tìm đường tâm đạo, trí đạo và sinh đạo – ngay trên điện thoại."),
        Triple("🔮", "Tử vi & Tarot mỗi ngày", "Báo cáo may mắn hằng ngày, tử vi theo cung và bộ bài Tarot 78 lá."),
        Triple("🪐", "Lá số chiêm tinh", "Lập lá số Vệ Đà từ ngày giờ và nơi sinh, xem độ hợp đôi với người ấy."),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.datePicker.maxDate = System.currentTimeMillis()
        b.datePicker.updateDate(2000, 0, 1)
        b.btnNext.setOnClickListener { next() }
        onBackPressedDispatcher.addCallback(this) { if (step > 0) { step--; render() } else finish() }
        render()
    }

    private fun next() {
        when {
            step < intro.size -> step++
            step == intro.size -> {
                if (b.etName.text.isNullOrBlank()) return toast("Hãy nhập tên của bạn")
                step++
            }
            else -> {
                val gender = when (b.chipsGender.checkedChipId) {
                    b.chipMale.id -> "Nam"
                    b.chipFemale.id -> "Nữ"
                    b.chipOther.id -> "Khác"
                    else -> ""
                }
                val date = LocalDate.of(b.datePicker.year, b.datePicker.month + 1, b.datePicker.dayOfMonth)
                Profile(name = b.etName.text.toString().trim(), gender = gender, birth = date.atTime(LocalTime.NOON)).save(this)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            }
        }
        render()
    }

    private fun render() {
        val isIntro = step < intro.size
        b.groupName.visibility = if (step == intro.size) View.VISIBLE else View.GONE
        b.datePicker.visibility = if (step > intro.size) View.VISIBLE else View.GONE
        when {
            isIntro -> intro[step].let { (e, t, body) -> b.tvEmoji.text = e; b.tvTitle.text = t; b.tvBody.text = body }
            step == intro.size -> { b.tvEmoji.text = "✍️"; b.tvTitle.text = "Bạn tên là gì?"; b.tvBody.text = "Tên giúp cá nhân hóa lời chào và kết quả của bạn." }
            else -> { b.tvEmoji.text = "🎂"; b.tvTitle.text = "Ngày sinh của bạn"; b.tvBody.text = "Dùng để xác định cung hoàng đạo và báo cáo hằng ngày." }
        }
        b.tvDots.text = (0..intro.size + 1).joinToString(" ") { if (it == step) "●" else "○" }
        b.btnNext.text = when {
            isIntro -> "Tiếp tục"
            step == intro.size -> "Tiếp tục"
            else -> "Bắt đầu"
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
