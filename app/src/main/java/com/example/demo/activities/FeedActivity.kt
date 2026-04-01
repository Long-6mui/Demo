package com.example.demo.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import android.util.Log

class FeedActivity : AppCompatActivity() {

    // ====== VIEW + AUTH + DATABASE LOCAL ======
    private lateinit var imgAvatar: ImageView   // Avatar người dùng trên thanh đầu
    lateinit var auth: FirebaseAuth            // Xác thực người dùng
    lateinit var dbHelper: DatabaseHelper      // Lấy dữ liệu user từ SQLite

    lateinit var recyclerView: RecyclerView    // Danh sách bài viết (feed)

    lateinit var edtPost: EditText             // Ô nhập nội dung post mới
    lateinit var btnPost: Button               // Nút đăng bài
    lateinit var imgPick: ImageView            // Ảnh chọn từ gallery

    // ====== LIST + ADAPTER DÙNG CHUNG CHO TOÀN ACTIVITY ======
    companion object {
        var list = mutableListOf<Post>()       // Danh sách tất cả post (mẫu + firebase)
        lateinit var adapter: PostAdapter      // Adapter của RecyclerView
    }

    // ====== KẾT NỐI FIREBASE ======
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var imageUri: Uri? = null                 // Lưu ảnh người dùng chọn

    // ====== CONTRACT CHỌN ẢNH TỪ GALLERY ======
    val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            imgPick.setImageURI(uri)          // Hiển thị ảnh vừa chọn lên UI
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        // ====== ÁNH XẠ VIEW ======
        recyclerView = findViewById(R.id.recyclerFeed)
        edtPost = findViewById(R.id.edtPost)
        btnPost = findViewById(R.id.btnPost)
        imgPick = findViewById(R.id.imgPick)

        // ====== ĐỒNG BỘ AVATAR TỪ SQLITE LÊN UI ======
        dbHelper = DatabaseHelper(this)
        imgAvatar = findViewById(R.id.avatarUser)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            val userData = dbHelper.getUserByUID(user.uid)
            if (userData != null) {
                if (userData.avatar.isEmpty()) {
                    imgAvatar.setImageResource(R.drawable.avtque)
                } else {
                    Glide.with(this).load(userData.avatar).into(imgAvatar)
                }
            }
        }

        // ====== TẠO 3 POST MẪU (CHỈ CHẠY 1 LẦN KHI LIST RỖNG) ======
        if(list.isEmpty()){
            list.add(Post("", R.drawable.ava1,"Anna",
                "Hôm nay mình nấu mì Ý 😚",3,
                "https://daubepgiadinh.vn/wp-content/uploads/2018/05/hinh-mi-y-ngon.jpg"))

            list.add(Post("", R.drawable.ava2,"John",
                "Gà rán siêu giòn tự làm ở nhà 🤤",5,
                "https://cdn.eva.vn//upload/3-2016/images/2016-07-22/uc-ga-kfc-mon-ngon-be-thich-uc-ga-kfc--6--1469194272-width500height375.jpg"))

            list.add(Post("", R.drawable.ava3,"Lisa",
                "Tự thưởng cho mình sau 1 ngày dài 😍",8,
                "https://images2.thanhnien.vn/528068263637045248/2023/2/14/6-pan-cake-16763820419171669639791.jpg"))
        }

        // ====== SETUP RECYCLERVIEW ======
        adapter = PostAdapter(list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ====== LOAD POST TỪ FIRESTORE (ADD THÊM, KHÔNG XÓA POST MẪU) ======
        loadPosts()

        // ====== SỰ KIỆN CHỌN ẢNH ======
        imgPick.setOnClickListener {
            pickImage.launch("image/*")
        }

        // ====== SỰ KIỆN ĐĂNG BÀI ======
        btnPost.setOnClickListener {
            val content = edtPost.text.toString()

            if (content.isNotEmpty()) {
                if(imageUri != null){
                    uploadImageCloudinary(content, imageUri!!) // Cloudinary thay cho Storage
                }else{
                    savePost(content,"") // Không có ảnh
                }
                edtPost.setText("")
                imageUri = null
                imgPick.setImageResource(R.drawable.daucong) // Reset icon ảnh
            }
        }

        // ====== NÚT QUAY LẠI ======
        findViewById<ImageView>(R.id.btnBackHome).setOnClickListener {
            finish()
        }
    }

    // ====== UPLOAD ẢNH LÊN CLOUDINARY ======
    fun uploadImageCloudinary(content: String, uri: Uri) {
        MediaManager.get().upload(uri)
            .option("folder", "posts") // lưu trong folder posts
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // Có thể hiển thị progress nếu muốn
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url")?.toString() ?: ""
                    Log.d("Cloudinary", "Upload success: $url")
                    savePost(content, url) // Lưu post với URL Cloudinary
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload error: ${error?.description}")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }



    // ====== LƯU POST VÀO LIST + FIRESTORE ======
    fun savePost(content: String, imageUrl: String){

        val post = Post(
            imgAvatar = R.drawable.ava5,
            name = "User",
            content = content,
            likes = 0,
            imageUrl = imageUrl
        )

        // Hiển thị ngay lên feed (không chờ Firebase)
        list.add(0, post)
        adapter.notifyDataSetChanged()

        // Lưu lên Firestore
        val data = hashMapOf(
            "name" to "User",
            "content" to content,
            "likes" to 0,
            "imageUrl" to imageUrl
        )

        db.collection("posts")
            .add(data)
            .addOnSuccessListener { document ->
                post.id = document.id   // Lưu id Firebase vào post
            }
    }

    // ====== LOAD POST TỪ FIRESTORE VÀ ADD THÊM VÀO LIST ======
    fun loadPosts(){

        db.collection("posts")
            .get()
            .addOnSuccessListener { result ->

                for (doc in result) {
                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id

                    // Tránh add trùng
                    val exists = list.any { it.id == post.id }
                    if(!exists){
                        list.add(post)
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}