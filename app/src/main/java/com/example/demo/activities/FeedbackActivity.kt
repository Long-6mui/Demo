package com.example.demo.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val btnBack = findViewById<ImageButton>(R.id.btnBackFeedback)
        val btnSend = findViewById<Button>(R.id.btnSendFeedback)
        val edtFeedback = findViewById<EditText>(R.id.edtFeedback)
        rvHistory = findViewById(R.id.rvFeedbackHistory)

        btnBack.setOnClickListener { finish() }

        // Setup RecyclerView
        adapter = UserFeedbackAdapter(feedbackList)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        btnSend.setOnClickListener {
            val text = edtFeedback.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập góp ý", Toast.LENGTH_SHORT).show()
            } else {
                sendFeedbackToFirestore(text, edtFeedback)
            }
        }

        loadFeedbackHistory()
    }

    private fun loadFeedbackHistory() {
        val userId = auth.currentUser?.uid ?: return

        // Lắng nghe thời gian thực
        db.collection("feedbacks")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                feedbackList.clear()
                snapshots?.forEach { doc ->
                    val feedback = doc.toObject(Feedback::class.java).copy(id = doc.id)
                    feedbackList.add(feedback)
                }
                // Cập nhật giao diện ngay lập tức
                adapter.notifyDataSetChanged()
            }
    }

    private fun sendFeedbackToFirestore(content: String, editText: EditText) {
        val user = auth.currentUser
        val userId = user?.uid ?: "anonymous"
        
        db.collection("Users").document(userId).get().addOnSuccessListener { document ->
            val userName = document.getString("name") ?: "Người dùng ẩn danh"
            
            val feedbackData = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "isInterested" to false,
                "adminReply" to ""
            )

            db.collection("feedbacks")
                .add(feedbackData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cảm ơn bạn đã góp ý ❤️", Toast.LENGTH_SHORT).show()
                    editText.setText("")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi gửi góp ý", Toast.LENGTH_SHORT).show()
                }
        }
    }
}