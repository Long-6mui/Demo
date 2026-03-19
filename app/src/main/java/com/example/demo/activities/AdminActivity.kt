package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val btnBack       = findViewById<ImageButton>(R.id.btnBack)
        val txtUserName   = findViewById<TextView>(R.id.txtUserName)
        val txtUserID     = findViewById<TextView>(R.id.txtUserID)
        val btnAddRecipe  = findViewById<LinearLayout>(R.id.btnAddRecipe)
        val btnEditRecipe = findViewById<LinearLayout>(R.id.btnEditRecipe)
        val btnDeleteRecipe = findViewById<LinearLayout>(R.id.btnDeleteRecipe)
        val btnManageUser = findViewById<LinearLayout>(R.id.btnManageUser)

        // Load thông tin admin từ Firestore
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val name  = doc.getString("nameUser") ?: "Admin"
                    val email = doc.getString("email") ?: ""
                    txtUserName.text = name
                    txtUserID.text   = email
                }
        }

        // Back → đăng xuất về Login
        btnBack.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Thêm công thức
        btnAddRecipe.setOnClickListener {
            // TODO: mở AddRecipeActivity
            Toast.makeText(this, "Thêm công thức", Toast.LENGTH_SHORT).show()
        }

        // Sửa công thức
        btnEditRecipe.setOnClickListener {
            // TODO: mở EditRecipeActivity
            Toast.makeText(this, "Sửa công thức", Toast.LENGTH_SHORT).show()
        }

        // xóa công thức
        btnDeleteRecipe.setOnClickListener {
            // TODO: mở DeleteRecipeActivity
            Toast.makeText(this, "Xoá công thức", Toast.LENGTH_SHORT).show()
        }

        // Quản lý user
        btnManageUser.setOnClickListener {
            // TODO: mở ManageUserActivity
            Toast.makeText(this, "Quản lý user", Toast.LENGTH_SHORT).show()
        }
    }
}