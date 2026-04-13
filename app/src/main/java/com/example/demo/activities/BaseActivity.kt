package com.example.demo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            // 1. Vẫn dùng SharedPreferences để áp dụng màu ngay lập tức khi mở app (không bị nháy trắng)
            val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
            val localDarkMode = sharedPreferences.getBoolean("DarkMode_$currentUserId", false)
            applyDarkMode(localDarkMode)

            // 2. Lắng nghe từ Firestore để đồng bộ giữa các thiết bị
            db.collection("Users").document(currentUserId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val remoteDarkMode = snapshot.getBoolean("isDarkMode") ?: false
                        if (remoteDarkMode != localDarkMode) {
                            // Cập nhật lại local và áp dụng nếu có sự thay đổi từ máy khác
                            sharedPreferences.edit().putBoolean("DarkMode_$currentUserId", remoteDarkMode).apply()
                            applyDarkMode(remoteDarkMode)
                        }
                    }
                }
        }
        super.onCreate(savedInstanceState)
    }

    private fun applyDarkMode(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}