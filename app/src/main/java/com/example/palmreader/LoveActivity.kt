package com.example.palmreader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.palmreader.databinding.ActivityLoveBinding

class LoveActivity : AppCompatActivity() {
    private lateinit var b: ActivityLoveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoveBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.etYou.setText(Profile.load(this).name)
        b.btnCheck.setOnClickListener {
            val you = b.etYou.text.toString()
            val partner = b.etPartner.text.toString()
            if (you.isBlank() || partner.isBlank()) {
                Toast.makeText(this, "Hãy nhập cả hai tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val score = loveScore(you, partner)
            val band = loveBand(score)
            b.tvScore.text = "$score%"
            b.tvBand.text = band.title
            b.tvBandText.text = band.text
        }
    }
}
