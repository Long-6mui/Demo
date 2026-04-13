package com.example.demo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminSettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_setting)

        // Ánh xạ View đúng ID trong XML
        val btnBack = findViewById<ImageButton>(R.id.btnBackAdminSetting)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode) // Dùng Switch thay vì SwitchCompat
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val currentUserId = auth.currentUser?.uid
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        if (currentUserId != null) {
            // 1. Hiển thị trạng thái Dark Mode theo UID
            val isDarkMode = sharedPreferences.getBoolean("DarkMode_$currentUserId", false)
            switchDarkMode.isChecked = isDarkMode

            // 2. Xử lý bật/tắt Dark Mode
            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit().putBoolean("DarkMode_$currentUserId", isChecked).apply()

                // Đồng bộ lên Cloud
                db.collection("Users").document(currentUserId).update("isDarkMode", isChecked)

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        // 3. Xử lý Đăng xuất
        btnLogout.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                // Cập nhật trạng thái Offline trước khi đăng xuất
                db.collection("Users").document(uid).update("isOnline", false)
                    .addOnCompleteListener {
                        auth.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
            } else {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}