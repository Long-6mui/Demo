package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var txtUserName: TextView
    private lateinit var txtUserID: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var btnLogout: LinearLayout
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var txtCountRecipes: TextView
    private lateinit var txtCountUsers: TextView
    private lateinit var txtCountFeedback: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        txtUserName = findViewById(R.id.txtUserName)
        txtUserID = findViewById(R.id.txtUserID)
        imgAvatar = findViewById(R.id.imgAvatar)
        btnLogout = findViewById(R.id.btnLogout)
        txtCountRecipes = findViewById(R.id.txtCountRecipes)
        txtCountUsers = findViewById(R.id.txtCountUsers)
        txtCountFeedback = findViewById(R.id.txtCountFeedback)

        editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadAdminInfo()
            }
        }
        loadAdminInfo()

        findViewById<CardView>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, editInfoActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        findViewById<CardView>(R.id.btnNewFeed)?.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.btnAddRecipe).setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.btnEditRecipe).setOnClickListener {
            val intent = Intent(this, ManageRecipesActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.btnManageUser).setOnClickListener {
            startActivity(Intent(this, ManageUserActivity::class.java))
        }

        findViewById<CardView>(R.id.btnManageFeedback).setOnClickListener {
            startActivity(Intent(this, ManageFeedbackActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                db.collection("Users").document(uid).update("isOnline", false)
                    .addOnCompleteListener {
                        auth.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
            } else {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        listenToStats()
    }

    private fun loadAdminInfo() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("Users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val hoTen = doc.getString("hoten") ?: doc.getString("name") ?: ""
                        val userName = doc.getString("name") ?: ""
                        val avatarUrl = doc.getString("avatar") ?: ""

                        txtUserName.text = hoTen
                        txtUserID.text = userName

                        if (avatarUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.avtque)
                                .into(imgAvatar)
                        }
                    }
                }
        }
    }

    private fun listenToStats() {
        // 1. Theo dõi số lượng CÔNG THỨC
        db.collection("recipes").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            txtCountRecipes.text = snapshots?.size()?.toString() ?: "0"
        }

        // 2. Theo dõi số lượng NGƯỜI DÙNG (Không tính Admin)
        db.collection("Users").addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) return@addSnapshotListener
            
            var userCount = 0
            for (doc in snapshots) {
                val role = doc.getString("role") ?: "user"
                if (role != "admin") {
                    userCount++
                }
            }
            txtCountUsers.text = userCount.toString()
        }

        // 3. Theo dõi số lượng GÓP Ý
        db.collection("feedbacks").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            txtCountFeedback.text = snapshots?.size()?.toString() ?: "0"
        }
    }
}