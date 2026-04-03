package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R
import android.widget.LinearLayout
import android.widget.ImageView
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.demo.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProfileActivity : AppCompatActivity() {

    private var imgAvatar: ImageView? = null
    private var txtUserID: TextView? = null
    private var txtUserName: TextView? = null
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid
        if (uid == null) {
            setContentView(R.layout.activity_profile)
            setupProfileUI()
            return
        }

        db.collection("Users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists() && doc.getString("role") == "admin") {
                    setContentView(R.layout.activity_admin)
                    setupAdminUI()
                } else {
                    setContentView(R.layout.activity_profile)
                    setupProfileUI()
                }
            }
            .addOnFailureListener {
                setContentView(R.layout.activity_profile)
                setupProfileUI()
            }
    }

    private fun setupAdminUI() {
        val btnBack         = findViewById<ImageButton>(R.id.btnBack)
        val tvName          = findViewById<TextView>(R.id.txtUserName)
        val tvEmail         = findViewById<TextView>(R.id.txtUserID)
        val btnAddRecipe    = findViewById<View>(R.id.btnAddRecipe)
        val btnEditRecipe   = findViewById<View>(R.id.btnEditRecipe)
        val btnManageUser   = findViewById<View>(R.id.btnManageUser)
        val btnLogout       = findViewById<View>(R.id.btnLogout)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("Users").document(uid).get()
                .addOnSuccessListener { doc ->
                    tvName?.text = doc.getString("name") ?: "Admin"
                    tvEmail?.text = doc.getString("email") ?: "admin@demo.com"
                }
        }

        btnBack?.setOnClickListener { finish() }
        btnAddRecipe?.setOnClickListener { startActivity(Intent(this, AddRecipeActivity::class.java)) }
        btnEditRecipe?.setOnClickListener { startActivity(Intent(this, EditRecipeActivity::class.java)) }
        btnManageUser?.setOnClickListener { startActivity(Intent(this, ManageUserActivity::class.java)) }
        
        btnLogout?.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupProfileUI() {
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val editProfile = findViewById<LinearLayout>(R.id.menuEditProfile)
        val menuFeedback = findViewById<LinearLayout>(R.id.menuFeedback)
        val menuCategory = findViewById<LinearLayout>(R.id.menuCategory)
        val menuContent = findViewById<LinearLayout>(R.id.menuAccountContent)
        val iconArrow = findViewById<ImageView>(R.id.iconArrow)
        
        // Gỡ bỏ phần RecyclerView trong code

        imgAvatar = findViewById(R.id.imgAvatar)
        txtUserName = findViewById(R.id.txtUserName)
        txtUserID = findViewById(R.id.txtUserID)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("Users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val hoten = document.getString("hoten") ?: ""
                        val name = document.getString("name") ?: ""
                        val avatarUrl = document.getString("avatar") ?: ""

                        txtUserName?.text = if (hoten.isNotEmpty()) hoten else name
                        txtUserID?.text = name

                        if (avatarUrl.isNotEmpty()) {
                            imgAvatar?.let { Glide.with(this).load(avatarUrl).placeholder(R.drawable.avtque).into(it) }
                        }
                    }
                }
        }

        btnBack?.setOnClickListener { finish() }
        btnLogout?.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        editProfile?.setOnClickListener { startActivity(Intent(this, editInfoActivity::class.java)) }
        menuFeedback?.setOnClickListener { startActivity(Intent(this, FeedbackActivity::class.java)) }

        menuCategory?.setOnClickListener {
            if (menuContent?.visibility == View.GONE) {
                menuContent.visibility = View.VISIBLE
                iconArrow?.rotation = 180f
            } else {
                menuContent?.visibility = View.GONE
                iconArrow?.rotation = 0f
            }
        }
    }
}