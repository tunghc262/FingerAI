package com.example.palmreader

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.palmreader.databinding.ActivityProfileBinding
import java.time.LocalDate
import java.time.LocalTime

/** Sửa hồ sơ; extra [EXTRA_KEY] chọn hồ sơ "của tôi" hay "người khác". */
class ProfileActivity : AppCompatActivity() {
    private lateinit var b: ActivityProfileBinding
    private lateinit var key: String
    private var date: LocalDate? = null
    private var time: LocalTime = LocalTime.NOON
    private var place: Net.Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)
        key = intent.getStringExtra(EXTRA_KEY) ?: Profile.KEY_ME
        if (key == Profile.KEY_OTHER) title = "Hồ sơ người khác"

        val p = Profile.load(this, key)
        b.etName.setText(p.name)
        when (p.gender) {
            "Nam" -> b.rbMale.isChecked = true
            "Nữ" -> b.rbFemale.isChecked = true
            "Khác" -> b.rbOther.isChecked = true
        }
        p.birth?.let { date = it.toLocalDate(); time = it.toLocalTime() }
        if (p.hasPlace) place = Net.Place(p.place, p.lat, p.lon, p.zone)
        render()

        b.btnDate.setOnClickListener {
            val d = date ?: LocalDate.of(2000, 1, 1)
            DatePickerDialog(this, { _, y, m, day -> date = LocalDate.of(y, m + 1, day); render() }, d.year, d.monthValue - 1, d.dayOfMonth)
                .apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }
        b.btnTime.setOnClickListener {
            TimePickerDialog(this, { _, h, min -> time = LocalTime.of(h, min); render() }, time.hour, time.minute, true).show()
        }
        b.btnSearch.setOnClickListener { search() }
        b.etPlace.setOnEditorActionListener { _, id, _ -> (id == EditorInfo.IME_ACTION_SEARCH).also { if (it) search() } }
        b.btnSave.setOnClickListener { save() }
    }

    private fun render() {
        b.btnDate.text = date?.let { "Ngày sinh: %02d/%02d/%d".format(it.dayOfMonth, it.monthValue, it.year) } ?: "Chọn ngày sinh"
        b.btnTime.text = "Giờ sinh: %02d:%02d".format(time.hour, time.minute)
        b.tvPlace.text = place?.let { "Nơi sinh: ${it.label} (${it.zone})" } ?: "Chưa chọn nơi sinh (cần cho lá số Vệ Đà)"
    }

    private fun search() {
        val q = b.etPlace.text.toString().trim()
        if (q.length < 2) return toast("Nhập ít nhất 2 ký tự")
        b.btnSearch.isEnabled = false
        background({ Net.searchPlace(q) }) { r ->
            b.btnSearch.isEnabled = true
            r.onFailure { toast("Không tìm được: ${it.message}") }
            r.onSuccess { list ->
                if (list.isEmpty()) return@onSuccess toast("Không có kết quả")
                AlertDialog.Builder(this)
                    .setTitle("Chọn nơi sinh")
                    .setItems(list.map { it.label }.toTypedArray()) { _, i -> place = list[i]; render() }
                    .show()
            }
        }
    }

    private fun save() {
        val name = b.etName.text.toString().trim()
        val d = date
        if (name.isEmpty()) return toast("Hãy nhập tên")
        if (d == null) return toast("Hãy chọn ngày sinh")
        val gender = when (b.rgGender.checkedRadioButtonId) {
            b.rbMale.id -> "Nam"
            b.rbFemale.id -> "Nữ"
            b.rbOther.id -> "Khác"
            else -> ""
        }
        val pl = place
        Profile(name, gender, d.atTime(time), pl?.label ?: "", pl?.lat ?: Double.NaN, pl?.lon ?: Double.NaN, pl?.zone ?: "").save(this, key)
        toast("Đã lưu")
        finish()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_KEY = "key"
    }
}
