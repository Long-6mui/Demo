package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileActivity : BaseActivity() {

    private var imgAvatar: ImageView? = null
    private var txtUserID: TextView? = null
    private var txtUserName: TextView? = null
    private var txtCountPosts: TextView? = null
    private var txtCountFavorites: TextView? = null
    private var txtCountSaved: TextView? = null
    
    private lateinit var editProfileLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        editProfileLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                reloadUserProfile()
            }
        }

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
        val btnAddRecipe    = findViewById<View>(R.id.btnAddRecipe)
        val btnEditRecipe   = findViewById<View>(R.id.btnEditRecipe)
        val btnManageUser   = findViewById<View>(R.id.btnManageUser)
        val btnLogout       = findViewById<View>(R.id.btnLogout)

        btnBack?.setOnClickListener { finish() }
        btnAddRecipe?.setOnClickListener { startActivity(Intent(this, AddRecipeActivity::class.java)) }
        btnEditRecipe?.setOnClickListener { startActivity(Intent(this, ManageRecipesActivity::class.java)) }
        btnManageUser?.setOnClickListener { startActivity(Intent(this, ManageUserActivity::class.java)) }

        btnLogout?.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                db.collection("Users").document(uid).update("isOnline", false)
                    .addOnCompleteListener {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
            } else {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
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
        val menuSetting = findViewById<LinearLayout>(R.id.menuSetting)
        val tabSaved = findViewById<LinearLayout>(R.id.tabSaved)

        txtCountPosts = findViewById(R.id.txtCountPosts)
        txtCountFavorites = findViewById(R.id.txtCountFavorites)
        txtCountSaved = findViewById(R.id.txtCountSaved)
        imgAvatar = findViewById(R.id.imgAvatar)
        txtUserName = findViewById(R.id.txtUserName)
        txtUserID = findViewById(R.id.txtUserID)

        val recyclerMyPosts = findViewById<RecyclerView>(R.id.recyclerMyPosts)
        val txtNoPost = findViewById<TextView>(R.id.txtNoPost)

        recyclerMyPosts.layoutManager = LinearLayoutManager(this)
        val postList = mutableListOf<Post>()
        val adapter = PostAdapter(postList)
        recyclerMyPosts.adapter = adapter

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 1. Load Bài đăng & Tổng Like (Yêu thích)
            db.collection("posts")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { documents, e ->
                    if (e != null) {
                        Log.e("ProfileActivity", "Error loading posts", e)
                        return@addSnapshotListener
                    }
                    
                    val currentPosts = mutableListOf<Post>()
                    var totalLikes = 0
                    
                    if (documents == null || documents.isEmpty) {
                        txtNoPost.visibility = View.VISIBLE
                        recyclerMyPosts.visibility = View.GONE
                        txtCountPosts?.text = "0"
                        txtCountFavorites?.text = "0"
                    } else {
                        txtNoPost.visibility = View.GONE
                        recyclerMyPosts.visibility = View.VISIBLE
                        
                        for (document in documents) {
                            val post = document.toObject(Post::class.java)
                            post.id = document.id
                            currentPosts.add(post)
                            totalLikes += post.likedBy.size
                        }
                        txtCountPosts?.text = currentPosts.size.toString()
                        txtCountFavorites?.text = totalLikes.toString()
                    }
                    adapter.updatePosts(currentPosts)
                }
            
            // 2. Load số lượng Đã lưu (Từ kho lưu SavedRecipes của User)
            db.collection("Users").document(currentUser.uid)
                .collection("SavedRecipes")
                .addSnapshotListener { snapshots, _ ->
                    txtCountSaved?.text = snapshots?.size()?.toString() ?: "0"
                }

            // 3. Load thông tin User
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
            val uid = auth.currentUser?.uid
            if (uid != null) {
                db.collection("Users").document(uid).update("isOnline", false)
                    .addOnCompleteListener {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
            } else {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        tabSaved?.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
        editProfile?.setOnClickListener {
            val intent = Intent(this, editInfoActivity::class.java)
            editProfileLauncher.launch(intent)
        }
        menuFeedback?.setOnClickListener { startActivity(Intent(this, FeedbackActivity::class.java)) }
        menuSetting?.setOnClickListener { startActivity(Intent(this, SettingActivity::class.java)) }

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

    private fun reloadUserProfile() {
        val currentUser = auth.currentUser ?: return
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
}