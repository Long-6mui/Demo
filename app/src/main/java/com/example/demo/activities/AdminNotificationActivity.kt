package com.example.demo.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R

class AdminNotificationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_notification)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val rvNotifications = findViewById<RecyclerView>(R.id.rvAdminNotifications)
        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmpty)

        btnBack.setOnClickListener { finish() }

        rvNotifications.layoutManager = LinearLayoutManager(this)

        // Dữ liệu mẫu dành riêng cho Admin
        // Trong thực tế, bạn có thể load từ Firebase collection "admin_notifications"
        val adminNotifications = listOf(
            NotificationModel(R.drawable.ic_person, "Người dùng mới 'Nguyễn Văn A' vừa đăng ký tài khoản.", "Vừa xong"),
            NotificationModel(R.drawable.ic_comment, "Có 5 góp ý mới về ứng dụng cần được phản hồi.", "15 phút trước"),
            NotificationModel(R.drawable.ic_category, "Hệ thống vừa cập nhật thêm 10 công thức mới từ người dùng.", "1 giờ trước"),
            NotificationModel(R.drawable.ic_favorite, "Bài viết 'Phở Bò' của bạn đạt mốc 100 lượt lưu.", "3 giờ trước"),
            NotificationModel(R.drawable.ic_notifications, "Nhắc nhở: Kiểm tra và duyệt các công thức mới đang chờ.", "5 giờ trước")
        )

        if (adminNotifications.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvNotifications.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
            val adapter = NotificationAdapter(adminNotifications)
            rvNotifications.adapter = adapter
        }
    }
}