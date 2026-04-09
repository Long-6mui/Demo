package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Feedback
import java.text.SimpleDateFormat
import java.util.*

class UserFeedbackAdapter(private val feedbackList: List<Feedback>) :
    RecyclerView.Adapter<UserFeedbackAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtContent: TextView = view.findViewById(R.id.txtUserFeedback)
        val txtTime: TextView = view.findViewById(R.id.txtFeedbackTime)
        val txtAdminReply: TextView = view.findViewById(R.id.txtAdminReply)
        val layoutAdminReply: androidx.cardview.widget.CardView = view.findViewById(R.id.layoutAdminReply)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val divider: View = view.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedback = feedbackList[position]

        holder.txtContent.text = feedback.content
        
        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        holder.txtTime.text = sdf.format(Date(feedback.timestamp))

        // Hiển thị phản hồi Admin ngay lập tức khi adminReply thay đổi
        if (feedback.adminReply.isNotEmpty()) {
            holder.layoutAdminReply.visibility = View.VISIBLE
            holder.divider.visibility = View.VISIBLE
            holder.txtAdminReply.text = feedback.adminReply
            holder.txtStatus.text = "Admin đã phản hồi ✅"
            holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        } else {
            holder.layoutAdminReply.visibility = View.GONE
            holder.divider.visibility = View.GONE
            holder.txtStatus.text = "Đang chờ phản hồi..."
            holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
        }
    }

    override fun getItemCount() = feedbackList.size
}