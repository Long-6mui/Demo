package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ── Category clicks → CategoryActivity ──
        val categories = mapOf(
            R.id.catDryFood  to Pair("Đồ Khô",      "🍚"),
            R.id.catSoup     to Pair("Đồ Nước",     "🍜"),
            R.id.catSnack    to Pair("Ăn Vặt",      "🧆"),
            R.id.catDessert  to Pair("Tráng Miệng", "🍮"),
            R.id.catGrill    to Pair("Nướng",        "🔥"),
            R.id.catVegan    to Pair("Chay",         "🥗"),
            R.id.catDrink    to Pair("Đồ Uống",     "🧋"),
        )

        categories.forEach { (id, data) ->
            findViewById<LinearLayout>(id).setOnClickListener {
                val intent = Intent(this, CategoryActivity::class.java)
                intent.putExtra("category", data.first)
                intent.putExtra("emoji", data.second)
                startActivity(intent)
            }
        }

        // Nút "Xem thêm"
        findViewById<LinearLayout>(R.id.catMore).setOnClickListener {
            // TODO: mở trang tất cả danh mục
        }

        // Nút "Xem tất cả" ở section yêu thích
        findViewById<TextView>(R.id.txtSeeAll).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        // ── Bottom Navigation ──
        findViewById<LinearLayout>(R.id.tabProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.tabSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
        val tabProfile = findViewById<LinearLayout>(R.id.tabProfile)

        tabProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val btnHome = findViewById<ImageButton>(R.id.btnHome)

        btnHome.setOnClickListener {

            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)

        }
    }
}