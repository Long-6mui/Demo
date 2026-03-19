package com.example.demo.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.R
import android.widget.LinearLayout
import android.widget.ImageView
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.Database.DatabaseHelper
import com.example.demo.adapters.RecipeAdapter
import com.example.demo.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgAvatar: ImageView
    lateinit var txtUserID: TextView
    lateinit var txtUserName: TextView
    lateinit var auth: FirebaseAuth
    lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val editProfile = findViewById<LinearLayout>(R.id.menuEditProfile)
        val menuFeedback = findViewById<LinearLayout>(R.id.menuFeedback)
        val menuCategory = findViewById<LinearLayout>(R.id.menuCategory)
        val menuContent = findViewById<LinearLayout>(R.id.menuAccountContent)
        val iconArrow = findViewById<ImageView>(R.id.iconArrow)
        val menuSetting = findViewById<LinearLayout>(R.id.menuSetting)
        val recyclerMyPosts = findViewById<RecyclerView>(R.id.recyclerMyPosts)

        //Đồng bộ avtar, tên, username
        dbHelper = DatabaseHelper(this)
        imgAvatar = findViewById(R.id.imgAvatar)
        txtUserName = findViewById(R.id.txtUserName)
        txtUserID = findViewById(R.id.txtUserID)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val userID = user.uid
            val userData = dbHelper.getUserByUID(userID)
            if (userData != null) {
                txtUserName.setText(userData.hoten)
                txtUserID.setText(userData.name)
                if (userData.avatar.isEmpty()) {
                    imgAvatar.setImageResource(R.drawable.avtque)
                } else {
                    Glide.with(this)
                        .load(userData.avatar)
                        .into(imgAvatar)
                }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

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

        menuSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

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