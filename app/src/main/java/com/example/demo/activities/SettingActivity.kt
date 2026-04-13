package com.example.demo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import com.example.demo.R

class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val btnBack = findViewById<ImageButton>(R.id.btnBackSetting)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
        val menuChangePassword = findViewById<LinearLayout>(R.id.menuChangePassword)

        // Lấy UID người dùng hiện tại
        val currentUserId = auth.currentUser?.uid
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        if (currentUserId != null) {
            // 1. Hiển thị trạng thái hiện tại dựa trên UID của người dùng
            val isDarkMode = sharedPreferences.getBoolean("DarkMode_$currentUserId", false)
            switchDarkMode.isChecked = isDarkMode

            // 2. Xử lý khi người dùng bật/tắt Switch
            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                // 1. Lưu local
                sharedPreferences.edit().putBoolean("DarkMode_$currentUserId", isChecked).apply()

                // 2. Lưu lên Firestore để đồng bộ
                db.collection("Users").document(currentUserId).update("isDarkMode", isChecked)

                // 3. Áp dụng ngay
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        btnBack.setOnClickListener { finish() }

        menuChangePassword.setOnClickListener {
            startActivity(Intent(this, changePasswordActivity::class.java))
        }
    }
}