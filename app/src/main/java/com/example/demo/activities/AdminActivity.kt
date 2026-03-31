package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // Thêm import này
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)



        val txtUserName   = findViewById<TextView>(R.id.txtUserName)
        val txtUserID     = findViewById<TextView>(R.id.txtUserID)
        val btnLogout     = findViewById<LinearLayout>(R.id.btnLogout)

        // Đổi từ LinearLayout thành CardView để khớp với XML
        val btnAddRecipe  = findViewById<CardView>(R.id.btnAddRecipe)
        val btnEditRecipe = findViewById<CardView>(R.id.btnEditRecipe)
        val btnManageFeedback = findViewById<CardView>(R.id.btnManageFeedback)
        val btnManageUser = findViewById<CardView>(R.id.btnManageUser)

        // Load thông tin admin
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("Users").document(uid) // Lưu ý chữ "Users" viết hoa hay thường cho khớp DB
                .get()
                .addOnSuccessListener { doc ->
                    txtUserName.text = doc.getString("nameUser") ?: "Admin"
                    txtUserID.text   = doc.getString("email") ?: "admin@demo.com"
                }
        }



        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<CardView>(R.id.btnAddRecipe).setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }
        findViewById<CardView>(R.id.btnEditRecipe).setOnClickListener {
            startActivity(Intent(this, EditRecipeActivity::class.java))
        }
        findViewById<CardView>(R.id.btnManageUser).setOnClickListener {
            startActivity(Intent(this, ManageUserActivity::class.java))
        }
        findViewById<CardView>(R.id.btnManageFeedback).setOnClickListener {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }
}