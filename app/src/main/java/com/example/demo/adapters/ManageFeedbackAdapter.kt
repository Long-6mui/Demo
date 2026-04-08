package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Feedback
import com.google.firebase.firestore.FirebaseFirestore

class ManageFeedbackAdapter(private val feedbackList: MutableList<Feedback>) :
    RecyclerView.Adapter<ManageFeedbackAdapter.FeedbackViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class FeedbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = view.findViewById(R.id.txtFeedbackUserName)
        val txtContent: TextView = view.findViewById(R.id.txtFeedbackContent)
        val btnInterested: Button = view.findViewById(R.id.btnInterested)
        val btnNotInterested: Button = view.findViewById(R.id.btnNotInterested)
        val viewIndicator: View = view.findViewById(R.id.viewInterestedIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]

        holder.txtUserName.text = feedback.userName
        holder.txtContent.text = feedback.content

        // Hiển thị trạng thái Quan tâm
        updateUI(holder, feedback.isInterested)

        holder.btnInterested.setOnClickListener {
            updateFeedbackStatus(feedback, true, holder)
        }

        holder.btnNotInterested.setOnClickListener {
            updateFeedbackStatus(feedback, false, holder)
        }

        // Khi click vào khung nội dung, có thể hiện toàn bộ text nếu nó bị cắt
        holder.itemView.setOnClickListener {
            if (holder.txtContent.maxLines == 2) {
                holder.txtContent.maxLines = 100
            } else {
                holder.txtContent.maxLines = 2
            }
        }
    }

    private fun updateFeedbackStatus(feedback: Feedback, interested: Boolean, holder: FeedbackViewHolder) {
        db.collection("feedbacks").document(feedback.id)
            .update("isInterested", interested)
            .addOnSuccessListener {
                feedback.isInterested = interested
                updateUI(holder, interested)
            }
    }

    private fun updateUI(holder: FeedbackViewHolder, isInterested: Boolean) {
        if (isInterested) {
            holder.viewIndicator.visibility = View.VISIBLE
            holder.btnInterested.alpha = 0.5f
            holder.btnNotInterested.alpha = 1.0f
        } else {
            holder.viewIndicator.visibility = View.GONE
            holder.btnInterested.alpha = 1.0f
            holder.btnNotInterested.alpha = 0.5f
        }
    }

    override fun getItemCount(): Int = feedbackList.size
}