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
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)

        if (currentUserId != null) {
            // Lấy cài đặt theo UID của từng người dùng/admin
            val isDarkMode = sharedPreferences.getBoolean("DarkMode_$currentUserId", false)
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            // Nếu logout hoặc chưa đăng nhập, trả về Light Mode mặc định
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        super.onCreate(savedInstanceState)
    }
}