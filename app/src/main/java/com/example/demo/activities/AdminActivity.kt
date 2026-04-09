package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.example.demo.R

class AdminActivity : BaseActivity() {

    private lateinit var txtUserName: TextView
    private lateinit var txtUserID: TextView
    private lateinit var imgAvatar: ImageView
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
        txtCountRecipes = findViewById(R.id.txtCountRecipes)
        txtCountUsers = findViewById(R.id.txtCountUsers)
        txtCountFeedback = findViewById(R.id.txtCountFeedback)

        editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadAdminInfo()
            }
        }
        loadAdminInfo()

        // Nút CHỈNH SỬA THÔNG TIN
        findViewById<CardView>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, editInfoActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        // Nút BÀI ĐĂNG (New Feed)
        findViewById<CardView>(R.id.btnNewFeed)?.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        // Nút THÊM MỚI
        findViewById<CardView>(R.id.btnAddRecipe).setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        // Nút QUẢN LÝ CÔNG THỨC
        findViewById<CardView>(R.id.btnEditRecipe).setOnClickListener {
            val intent = Intent(this, ManageRecipesActivity::class.java)
            startActivity(intent)
        }

        // Nút QUẢN LÝ USER
        findViewById<CardView>(R.id.btnManageUser).setOnClickListener {
            startActivity(Intent(this, ManageUserActivity::class.java))
        }

        // Nút QUẢN LÝ GÓP Ý
        findViewById<CardView>(R.id.btnManageFeedback).setOnClickListener {
            startActivity(Intent(this, ManageFeedbackActivity::class.java))
        }

        // Nút CÀI ĐẶT ADMIN (Chứa Dark Mode và Đăng xuất)
        findViewById<CardView>(R.id.btnAdminSetting).setOnClickListener {
            startActivity(Intent(this, AdminSettingActivity::class.java))
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
        db.collection("recipes").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            txtCountRecipes.text = snapshots?.size()?.toString() ?: "0"
        }

        db.collection("Users").addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) return@addSnapshotListener
            var userCount = 0
            for (doc in snapshots) {
                if (doc.getString("role") != "admin") userCount++
            }
            txtCountUsers.text = userCount.toString()
        }

        db.collection("feedbacks").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            txtCountFeedback.text = snapshots?.size()?.toString() ?: "0"
        }
    }
}