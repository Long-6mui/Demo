package com.example.demo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.adapters.ManageFeedbackAdapter
import com.example.demo.models.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ManageFeedbackActivity : AppCompatActivity() {

    private lateinit var rvFeedbacks: RecyclerView
    private lateinit var adapter: ManageFeedbackAdapter
    private val feedbackList = mutableListOf<Feedback>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_feedback)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarManageFeedback)
        toolbar.setNavigationOnClickListener { finish() }

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

                feedbackList.clear()
                value?.forEach { doc ->
                    val feedback = doc.toObject(Feedback::class.java)
                    feedback.id = doc.id
                    feedbackList.add(feedback)
                }
                adapter.notifyDataSetChanged()
            }
    }
}