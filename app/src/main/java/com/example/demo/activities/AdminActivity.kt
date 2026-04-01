package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val txtUserName = findViewById<TextView>(R.id.txtUserName)
        val txtUserID = findViewById<TextView>(R.id.txtUserID)
        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)



        // Load thông tin admin từ Firestore
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("Users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    txtUserName.text = doc.getString("name") ?: "Admin"
                    txtUserID.text = doc.getString("email") ?: "admin@demo.com"
                }
        }

        // Nút THÊM MỚI: Mở AddRecipeActivity
        findViewById<CardView>(R.id.btnAddRecipe).setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        // Nút QUẢN LÝ (SỬA/XÓA): Mở ManageRecipesActivity (Nơi chứa danh sách món)
        findViewById<CardView>(R.id.btnEditRecipe).setOnClickListener {
            val intent = Intent(this, ManageRecipesActivity::class.java)
            startActivity(intent)
        }

        // Nút QUẢN LÝ USER
        findViewById<CardView>(R.id.btnManageUser).setOnClickListener {
            // Mở màn hình quản lý User nếu mày đã có file ManageUserActivity
            startActivity(Intent(this, ManageUserActivity::class.java))
            Toast.makeText(this, "Tính năng quản lý User", Toast.LENGTH_SHORT).show()
        }

        //Nút Quản Lý Góp Ý
        findViewById<CardView>(R.id.btnManageFeedback).setOnClickListener {
            // Mở màn hình quản lý User nếu mày đã có file ManageUserActivity
            //startActivity(Intent(this, ManageFeedbackActivity::class.java))
            Toast.makeText(this, "Tính năng quản lý góp ý đang phát triển", Toast.LENGTH_SHORT).show()
        }

        // Đăng xuất
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}