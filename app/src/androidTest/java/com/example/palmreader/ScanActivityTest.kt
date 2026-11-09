package com.example.palmreader

import android.Manifest
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Mở/đóng màn camera nhiều lần: trước đây crash vì hai HandLandmarker được nạp song song. */
@RunWith(AndroidJUnit4::class)
class ScanActivityTest {
    @get:Rule
    val camera: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Test
    fun openAndCloseCameraScreenRepeatedly() {
        repeat(3) {
            ActivityScenario.launch(ScanActivity::class.java).use { s ->
                Thread.sleep(5000) // nạp model + vài khung hình hướng dẫn trực tiếp
                s.moveToState(Lifecycle.State.DESTROYED)
            }
        }
        Thread.sleep(3000) // để luồng nền đóng model xong; crash ở đây sẽ làm hỏng tiến trình test
    }
}
