package com.example.palmreader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.palmreader.databinding.ActivityPalmGuideBinding

class PalmGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityPalmGuideBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnStart.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
        }
    }
}
