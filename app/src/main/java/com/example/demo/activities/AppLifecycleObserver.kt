package com.example.demo.activities

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppLifecycleObserver : DefaultLifecycleObserver {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onStart(owner: LifecycleOwner) {
        // Khi app mở (Foreground)
        updateStatus(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        // Khi app đóng/vào nền (Background)
        updateStatus(false)
    }

    private fun updateStatus(isOnline: Boolean) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val data = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to com.google.firebase.Timestamp.now() // Lưu thời điểm cuối cùng
            )
            db.collection("Users").document(uid).update(data)
        }
    }
}