package com.example.demo.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.demo.R

class SettingActivity : AppCompatActivity() {

    private lateinit var switchNotification: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val btnBack = findViewById<ImageButton>(R.id.btnBackSetting)
        val btnChangePassword = findViewById<LinearLayout>(R.id.menuChangePassword)

        switchNotification = findViewById(R.id.switchNotification)
        switchDarkMode = findViewById(R.id.switchDarkMode)

        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)

        // Load trạng thái đã lưu
        switchNotification.isChecked = sharedPref.getBoolean("notification", true)
        switchDarkMode.isChecked = sharedPref.getBoolean("darkmode", false)

        btnBack.setOnClickListener {
            finish()
        }

        // bật tắt thông báo
        switchNotification.setOnCheckedChangeListener { _, isChecked ->

            sharedPref.edit().putBoolean("notification", isChecked).apply()

            if (isChecked) {
                Toast.makeText(this,"Đã bật thông báo 🔔",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Đã tắt thông báo 🔕",Toast.LENGTH_SHORT).show()
            }

        }

        // Dark mode
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->

            sharedPref.edit().putBoolean("darkmode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

        }

        // đổi mật khẩu
        val menuChangePassword = findViewById<LinearLayout>(R.id.menuChangePassword)
        btnChangePassword.setOnClickListener {
            val intent = Intent(this, changePasswordActivity::class.java)
            startActivity(intent)
        }
    }
}