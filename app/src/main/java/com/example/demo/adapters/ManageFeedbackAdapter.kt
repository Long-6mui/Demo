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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedback = feedbackList[position]
        val context = holder.itemView.context

        holder.txtUserName.text = feedback.userName
        holder.txtContent.text = feedback.content

        // Hiển thị trạng thái Quan tâm
        holder.indicator.visibility = if (feedback.isInterested) View.VISIBLE else View.GONE

        // Hiển thị phản hồi cũ nếu có
        if (feedback.adminReply.isNotEmpty()) {
            holder.txtAdminReply.text = "Admin phản hồi: ${feedback.adminReply}"
            holder.txtAdminReply.visibility = View.VISIBLE
        } else {
            holder.txtAdminReply.visibility = View.GONE
        }

        // Nút Quan tâm
        holder.btnInterested.setOnClickListener {
            FirebaseFirestore.getInstance().collection("feedbacks")
                .document(feedback.id)
                .update("isInterested", true)
        }

        // Nút Không quan tâm/Bỏ qua
        holder.btnNotInterested.setOnClickListener {
            FirebaseFirestore.getInstance().collection("feedbacks")
                .document(feedback.id)
                .update("isInterested", false)
        }

        // Gửi phản hồi
        holder.btnSendReply.setOnClickListener {
            val replyText = holder.edtReply.text.toString().trim()
            if (replyText.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập phản hồi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance().collection("feedbacks")
                .document(feedback.id)
                .update("adminReply", replyText)
                .addOnSuccessListener {
                    Toast.makeText(context, "Đã phản hồi thành công!", Toast.LENGTH_SHORT).show()
                    holder.edtReply.setText("")
                }
        }
    }

    override fun getItemCount() = feedbackList.size
}