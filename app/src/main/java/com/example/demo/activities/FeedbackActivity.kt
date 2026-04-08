package com.example.demo.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.demo.R

class FeedbackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val btnBack = findViewById<ImageButton>(R.id.btnBackFeedback)
        val btnSend = findViewById<Button>(R.id.btnSendFeedback)
        val edtFeedback = findViewById<EditText>(R.id.edtFeedback)

        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            val text = edtFeedback.text.toString().trim()

            if (text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập góp ý", Toast.LENGTH_SHORT).show()
            } else {
                sendFeedbackToFirestore(text, edtFeedback)
            }
        }
    }

    private fun sendFeedbackToFirestore(content: String, editText: EditText) {
        val user = auth.currentUser
        val userId = user?.uid ?: "anonymous"

        // Lấy tên người dùng nếu có
        db.collection("Users").document(userId).get().addOnSuccessListener { document ->
            val userName = document.getString("name") ?: "Người dùng ẩn danh"

            val feedback = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "isInterested" to false
            )

            db.collection("feedbacks")
                .add(feedback)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cảm ơn bạn đã góp ý ❤️", Toast.LENGTH_SHORT).show()
                    editText.setText("")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi gửi góp ý, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                }
        }
    }
}