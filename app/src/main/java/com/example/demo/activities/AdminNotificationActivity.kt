package com.example.demo.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.adapters.NotificationAdapter
import com.example.demo.models.Notification
import com.google.firebase.firestore.Query

class AdminNotificationActivity : BaseActivity() {

    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_notification)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val rvNotifications = findViewById<RecyclerView>(R.id.rvAdminNotifications)
        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmpty)

        btnBack.setOnClickListener { finish() }

        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(notificationList)
        rvNotifications.adapter = adapter

        // Lắng nghe thông báo hệ thống dành cho Admin
        startListeningAdminNotifications(layoutEmpty, rvNotifications)
    }

    private fun startListeningAdminNotifications(layoutEmpty: LinearLayout, rv: RecyclerView) {
        db.collection("admin_notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots != null) {
                    notificationList.clear()
                    for (doc in snapshots) {
                        val noti = doc.toObject(Notification::class.java)
                        noti.id = doc.id
                        notificationList.add(noti)
                    }

                    // Cập nhật giao diện
                    if (notificationList.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                        rv.visibility = View.GONE
                    } else {
                        layoutEmpty.visibility = View.GONE
                        rv.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                }
            }
    }
}