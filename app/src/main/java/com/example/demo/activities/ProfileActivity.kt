package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.R
import android.widget.LinearLayout
import android.widget.ImageView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.Recipe
import com.example.demo.adapters.RecipeAdapter

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Set layout mặc định trước
        setContentView(R.layout.activity_profile)

        val uid = com.google.firebase.auth.FirebaseAuth
            .getInstance().currentUser?.uid

        if (uid == null) {
            setupProfileUI()
            return
        }

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("Users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "user"
                android.util.Log.d("PROFILE", "role = $role")

                if (role == "admin") {
                    setContentView(R.layout.activity_admin)
                    setupAdminUI()
                } else {
                    setupProfileUI()
                }
            }
            .addOnFailureListener {
                android.util.Log.e("PROFILE", "Lỗi: ${it.message}")
                setupProfileUI()
            }
    }

    private fun setupAdminUI() {
        val btnBack         = findViewById<ImageButton>(R.id.btnBack)
        val txtUserName     = findViewById<TextView>(R.id.txtUserName)
        val txtUserID       = findViewById<TextView>(R.id.txtUserID)
        val btnAddRecipe    = findViewById<LinearLayout>(R.id.btnAddRecipe)
        val btnEditRecipe   = findViewById<LinearLayout>(R.id.btnEditRecipe)
        val btnDeleteRecipe = findViewById<LinearLayout>(R.id.btnDeleteRecipe)
        val btnManageUser   = findViewById<LinearLayout>(R.id.btnManageUser)

        // Load thông tin admin
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    txtUserName.text = doc.getString("nameUser") ?: "Admin"
                    txtUserID.text   = doc.getString("email") ?: "admin@demo.com"
                }
        }

        btnBack.setOnClickListener {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnAddRecipe.setOnClickListener {
            Toast.makeText(this, "Thêm công thức", Toast.LENGTH_SHORT).show()
        }
        btnEditRecipe.setOnClickListener {
            Toast.makeText(this, "Sửa công thức", Toast.LENGTH_SHORT).show()
        }
        btnDeleteRecipe.setOnClickListener {
            Toast.makeText(this, "Xoá công thức", Toast.LENGTH_SHORT).show()
        }
        btnManageUser.setOnClickListener {
            Toast.makeText(this, "Quản lý user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupProfileUI() {
        // Paste toàn bộ code cũ của ProfileActivity vào đây
        val btnBack     = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout   = findViewById<Button>(R.id.btnLogout)
        val editProfile = findViewById<LinearLayout>(R.id.menuEditProfile)
        val menuFeedback = findViewById<LinearLayout>(R.id.menuFeedback)
        val menuCategory = findViewById<LinearLayout>(R.id.menuCategory)
        val menuContent  = findViewById<LinearLayout>(R.id.menuAccountContent)
        val iconArrow    = findViewById<ImageView>(R.id.iconArrow)

        btnBack.setOnClickListener { finish() }

        btnLogout.setOnClickListener {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        editProfile.setOnClickListener {
            startActivity(Intent(this, editInfoActivity::class.java))
        }

        menuFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }

        menuCategory.setOnClickListener {
            if (menuContent.visibility == View.GONE) {
                menuContent.visibility = View.VISIBLE
                iconArrow.rotation = 180f
            } else {
                menuContent.visibility = View.GONE
                iconArrow.rotation = 0f
            }
        }

        findViewById<LinearLayout>(R.id.menuSetting).setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        val recyclerMyPosts = findViewById<RecyclerView>(R.id.recyclerMyPosts)
        recyclerMyPosts.isNestedScrollingEnabled = false
        val list = listOf(
            Recipe(R.drawable.goicuon, "Gỏi Cuốn Miền Tây", "Trái Banh"),
            Recipe(R.drawable.garan, "Gà Rán", "Trái Banh"),
            Recipe(R.drawable.cua, "Cua Rang Me", "Trái Banh")
        )
        recyclerMyPosts.layoutManager = GridLayoutManager(this, 2)
        recyclerMyPosts.adapter = RecipeAdapter(list)
    }

}