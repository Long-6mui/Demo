package com.example.demo.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.demo.R
import com.example.demo.adapters.UserFeedbackAdapter
import com.example.demo.models.Feedback
import com.google.firebase.firestore.Query

class FeedbackActivity : BaseActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: UserFeedbackAdapter
    private val feedbackList = mutableListOf<Feedback>()
    private lateinit var txtNoFeedbackHistory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val btnBack = findViewById<ImageButton>(R.id.btnBackFeedback)
        val btnSend = findViewById<Button>(R.id.btnSendFeedback)
        val edtFeedback = findViewById<EditText>(R.id.edtFeedback)
        rvHistory = findViewById(R.id.rvFeedbackHistory)
        txtNoFeedbackHistory = findViewById(R.id.txtNoFeedbackHistory)

        btnBack.setOnClickListener { finish() }

        adapter = UserFeedbackAdapter(feedbackList)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        btnSend.setOnClickListener {
            val content = edtFeedback.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendFeedback(content, edtFeedback)
        }

        startListeningFeedback()
    }

    private fun startListeningFeedback() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("feedbacks")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    feedbackList.clear()
                    for (doc in snapshots) {
                        val fb = doc.toObject(Feedback::class.java)
                        fb.id = doc.id
                        feedbackList.add(fb)
                    }
                    
                    // HIỂN THỊ THÔNG BÁO NẾU TRỐNG
                    if (feedbackList.isEmpty()) {
                        txtNoFeedbackHistory.visibility = View.VISIBLE
                        rvHistory.visibility = View.GONE
                    } else {
                        txtNoFeedbackHistory.visibility = View.GONE
                        rvHistory.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                }
            }
    }

    private fun sendFeedback(content: String, edt: EditText) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Users").document(uid).get().addOnSuccessListener { document ->
            val hoten = document.getString("hoten") ?: ""
            val name = document.getString("name") ?: ""
            val fromName = if (hoten.isNotEmpty()) hoten else name
            
            val feedbackData = hashMapOf(
                "userId" to uid,
                "userName" to fromName,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "isInterested" to false,
                "adminReply" to ""
            )

            db.collection("feedbacks").add(feedbackData).addOnSuccessListener {
                val adminNoti = hashMapOf(
                    "fromUserId" to uid,
                    "fromUserName" to fromName,
                    "type" to "feedback",
                    "content" to "đã gửi một góp ý mới.",
                    "timestamp" to System.currentTimeMillis(),
                    "seen" to false
                )
                db.collection("admin_notifications").add(adminNoti)

                Toast.makeText(this, "Cảm ơn bạn đã góp ý ❤️", Toast.LENGTH_SHORT).show()
                edt.setText("")
            }
        }
    }
}