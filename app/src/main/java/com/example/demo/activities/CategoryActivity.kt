package com.example.demo.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R

class CategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Nhận tên danh mục từ MainActivity
        val categoryName = intent.getStringExtra("category") ?: "Danh mục"
        val emoji = intent.getStringExtra("emoji") ?: "🍽"

        findViewById<TextView>(R.id.txtCategoryTitle).text = categoryName
        findViewById<TextView>(R.id.txtCategoryEmoji).text = emoji

        // Nút back
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}