package com.example.demo.activities

import android.os.Bundle
import android.util.Log
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

class NotificationActivity : BaseActivity() {

    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<Notification>()
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        findViewById<ImageButton>(R.id.btnBackNotification)?.setOnClickListener { finish() }

        layoutEmpty = findViewById(R.id.layoutEmpty)
        recycler = findViewById(R.id.recyclerNotification)
        
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(notificationList)
        recycler.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("notifications")
            .whereEqualTo("toUserId", currentUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("NotificationError", "Lỗi tải thông báo: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    notificationList.clear()
                    for (doc in snapshots) {
                        val noti = doc.toObject(Notification::class.java)
                        noti.id = doc.id
                        notificationList.add(noti)
                    }

                    // HIỂN THỊ EMPTY STATE NẾU KHÔNG CÓ THÔNG BÁO
                    if (notificationList.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                    } else {
                        layoutEmpty.visibility = View.GONE
                        recycler.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                    Log.d("NotificationSuccess", "Đã tải ${notificationList.size} thông báo")
                }
            }
    }
}