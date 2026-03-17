package com.example.demo.activities

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Nút quay lại
        val btnBack = findViewById<ImageButton>(R.id.btnBackNotification)

        btnBack.setOnClickListener {
            finish()
        }

        // RecyclerView
        val recycler = findViewById<RecyclerView>(R.id.recyclerNotification)

        recycler.layoutManager = LinearLayoutManager(this)

        // Dữ liệu demo
        val list = listOf(
            NotificationModel(R.drawable.login,"Nam đã thích bài viết của bạn","2 phút trước"),
            NotificationModel(R.drawable.login,"Lan đã bình luận bài viết của bạn","5 phút trước"),
            NotificationModel(R.drawable.login,"Hùng đã thích bài viết của bạn","10 phút trước"),
            NotificationModel(R.drawable.login,"Trang đã theo dõi bạn","1 giờ trước")
        )

        val adapter = NotificationAdapter(list)

        recycler.adapter = adapter
    }
}