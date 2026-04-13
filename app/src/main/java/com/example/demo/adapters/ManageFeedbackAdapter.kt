package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Feedback
import com.google.firebase.firestore.FirebaseFirestore

class ManageFeedbackAdapter(private val feedbackList: List<Feedback>) :
    RecyclerView.Adapter<ManageFeedbackAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = view.findViewById(R.id.txtFeedbackUserName)
        val txtContent: TextView = view.findViewById(R.id.txtFeedbackContent)
        val txtAdminReply: TextView = view.findViewById(R.id.txtAdminReply)
        val edtReply: EditText = view.findViewById(R.id.edtAdminReply)
        val btnSendReply: Button = view.findViewById(R.id.btnSendReply)
        val btnInterested: Button = view.findViewById(R.id.btnInterested)
        val btnNotInterested: Button = view.findViewById(R.id.btnNotInterested)
        val indicator: View = view.findViewById(R.id.viewInterestedIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feedback, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedback = feedbackList[position]
        val context = holder.itemView.context
        val db = FirebaseFirestore.getInstance()

        holder.txtUserName.text = feedback.userName
        holder.txtContent.text = feedback.content
        holder.indicator.visibility = if (feedback.isInterested) View.VISIBLE else View.GONE

        if (feedback.adminReply.isNotEmpty()) {
            holder.txtAdminReply.text = "Phản hồi: ${feedback.adminReply}"
            holder.txtAdminReply.visibility = View.VISIBLE
        } else {
            holder.txtAdminReply.visibility = View.GONE
        }

        holder.btnInterested.setOnClickListener {
            db.collection("feedbacks").document(feedback.id).update("isInterested", true)
        }

        holder.btnNotInterested.setOnClickListener {
            db.collection("feedbacks").document(feedback.id).update("isInterested", false)
        }

        holder.btnSendReply.setOnClickListener {
            val replyText = holder.edtReply.text.toString().trim()
            if (replyText.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập phản hồi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Cập nhật nội dung phản hồi vào góp ý
            db.collection("feedbacks").document(feedback.id)
                .update("adminReply", replyText)
                .addOnSuccessListener {
                    
                    // 2. PHÁT THÔNG BÁO CHO USER
                    val userNoti = hashMapOf(
                        "fromUserId" to "admin_system",
                        "fromUserName" to "Admin",
                        "toUserId" to feedback.userId,
                        "postId" to feedback.id,
                        "type" to "admin_reply",
                        "content" to replyText,
                        "timestamp" to System.currentTimeMillis(),
                        "seen" to false
                    )
                    db.collection("notifications").add(userNoti)

                    Toast.makeText(context, "Đã phản hồi và gửi thông báo!", Toast.LENGTH_SHORT).show()
                    holder.edtReply.setText("")
                }
        }
    }

    override fun getItemCount() = feedbackList.size
}