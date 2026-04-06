package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R
import android.widget.LinearLayout
import android.widget.ImageView
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var editProfileLauncher: androidx.activity.result.ActivityResultLauncher<Intent>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()

        editProfileLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                reloadUserProfile()        // Cập nhật lại thông tin
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
        val menuSetting = findViewById<LinearLayout>(R.id.menuSetting)

        //Code lấy bài viết
        val recyclerMyPosts = findViewById<RecyclerView>(R.id.recyclerMyPosts)
        val txtNoPost = findViewById<TextView>(R.id.txtNoPost)

        // 1. Cấu hình RecyclerView
        recyclerMyPosts.layoutManager = LinearLayoutManager(this)
        val postList = mutableListOf<Post>()
        val adapter = PostAdapter(postList)
        recyclerMyPosts.adapter = adapter

        // 2. Lấy dữ liệu từ Firestore
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Truy vấn các bài đăng có userId trùng với UID người dùng hiện tại
            db.collection("posts")
                .whereEqualTo("userId", currentUser.uid)

                // Thêm dòng này để sắp xếp bài viết của chính mình từ mới đến cũ
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

                .get()
                .addOnSuccessListener { documents ->
                    postList.clear()

                    // Kiểm tra nếu không có tài liệu nào trả về
                    if (documents.isEmpty) {
                        txtNoPost.visibility = View.VISIBLE
                        recyclerMyPosts.visibility = View.GONE
                    } else {
                        txtNoPost.visibility = View.GONE
                        recyclerMyPosts.visibility = View.VISIBLE

                        for (document in documents) {
                            val post = document.toObject(Post::class.java)
                            // Gán ID document vào object để phục vụ Like/Edit/Delete
                            post.id = document.id
                            postList.add(post)
                    }
                        // Cập nhật số lượng bài viết lên UI (ví dụ số 12)
                        val txtCountPosts = findViewById<TextView>(R.id.txtCountPosts)
                        txtCountPosts?.text = postList.size.toString()
                    }
                    adapter.notifyDataSetChanged()

                    // (Tùy chọn) Cập nhật số lượng bài đăng lên UI (số 12 trong layout)
                    // findViewById<TextView>(R.id.txtCountPosts).text = postList.size.toString()
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileActivity", "Error loading posts", e)
                }



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
                                imgAvatar?.let {
                                    Glide.with(this).load(avatarUrl).placeholder(R.drawable.avtque)
                                        .into(it)
                                }
                            }
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
            editProfile?.setOnClickListener {
                val intent = Intent(this, editInfoActivity::class.java)
                editProfileLauncher.launch(intent)
            }
            menuFeedback?.setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        FeedbackActivity::class.java
                    )
                )
            }
            menuSetting?.setOnClickListener {
                startActivity(Intent(this, SettingActivity::class.java))
            }

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




    // Thêm hàm reload thông tin (sẽ gọi lại khi quay về từ edit)
    private fun reloadUserProfile() {
        val currentUser = auth.currentUser ?: return

        // Reload thông tin cá nhân
        db.collection("Users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val hoten = document.getString("hoten") ?: ""
                    val name = document.getString("name") ?: ""
                    val avatarUrl = document.getString("avatar") ?: ""

                    txtUserName?.text = if (hoten.isNotEmpty()) hoten else name
                    txtUserID?.text = name

                    if (avatarUrl.isNotEmpty()) {
                        imgAvatar?.let {
                            Glide.with(this).load(avatarUrl)
                                .placeholder(R.drawable.avtque)
                                .into(it)
                        }
                    }
                }
            }
        loadMyPosts()
    }

    private fun loadMyPosts() {
        val recyclerMyPosts = findViewById<RecyclerView>(R.id.recyclerMyPosts) ?: return
        val txtNoPost = findViewById<TextView>(R.id.txtNoPost) ?: return
        val txtCountPosts = findViewById<TextView>(R.id.txtCountPosts)

        val currentUser = auth.currentUser ?: return

        db.collection("posts")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val postList = mutableListOf<Post>()

                if (documents.isEmpty) {
                    txtNoPost.visibility = View.VISIBLE
                    recyclerMyPosts.visibility = View.GONE
                } else {
                    txtNoPost.visibility = View.GONE
                    recyclerMyPosts.visibility = View.VISIBLE

                    for (document in documents) {
                        val post = document.toObject(Post::class.java)
                        post.id = document.id
                        postList.add(post)
                    }
                }

                // Cập nhật adapter
                val adapter = recyclerMyPosts.adapter as? PostAdapter
                adapter?.updatePosts(postList)   // dùng hàm mới thêm

                // Cập nhật số lượng bài viết
                txtCountPosts?.text = postList.size.toString()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Lỗi load bài viết", e)
            }
    }

}