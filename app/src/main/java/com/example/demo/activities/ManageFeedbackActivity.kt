package com.example.demo.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.adapters.ManageFeedbackAdapter
import com.example.demo.models.Feedback
import com.google.firebase.firestore.Query

class ManageFeedbackActivity : BaseActivity() {

    private lateinit var rvFeedbacks: RecyclerView
    private lateinit var adapter: ManageFeedbackAdapter
    private val feedbackList = mutableListOf<Feedback>()
    private lateinit var txtNoFeedback: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_feedback)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarManageFeedback)
        toolbar.setNavigationOnClickListener { finish() }

        txtNoFeedback = findViewById(R.id.txtNoFeedback)
        rvFeedbacks = findViewById(R.id.rvFeedbacks)
        rvFeedbacks.layoutManager = LinearLayoutManager(this)
        adapter = ManageFeedbackAdapter(feedbackList)
        rvFeedbacks.adapter = adapter

        loadFeedbacks()
    }

    private fun loadFeedbacks() {
        db.collection("feedbacks")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                if (value != null) {
                    feedbackList.clear()
                    value.forEach { doc ->
                        val feedback = doc.toObject(Feedback::class.java)
                        feedback.id = doc.id
                        feedbackList.add(feedback)
                    }

                    // HIỂN THỊ THÔNG BÁO NẾU TRỐNG
                    if (feedbackList.isEmpty()) {
                        txtNoFeedback.visibility = View.VISIBLE
                        rvFeedbacks.visibility = View.GONE
                    } else {
                        txtNoFeedback.visibility = View.GONE
                        rvFeedbacks.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                }
            }
    }
}