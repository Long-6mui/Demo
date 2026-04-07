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
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner


class FeedActivity : BaseActivity() {

    // ====== VIEW + AUTH ======
    private lateinit var avatarUser: ImageView   // Avatar người dùng trên thanh đầu

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

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())


        // ====== ÁNH XẠ VIEW ======
        recyclerView = findViewById(R.id.recyclerFeed)
        edtPost = findViewById(R.id.edtPost)
        btnPost = findViewById(R.id.btnPost)
        imgPick = findViewById(R.id.imgPick)

        // ====== ĐỒNG BỘ AVATAR TỪ FIREBASE LÊN UI ======
        avatarUser = findViewById(R.id.avatarUser)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("Users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val avatarUrl = document.getString("avatar") ?: ""

                        if (avatarUrl.isNotEmpty()) {
                            avatarUser?.let { Glide.with(this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.avtque)
                                .into(it) }
                        }
                    }
                }
        }

        // ====== SETUP RECYCLERVIEW ======
        adapter = PostAdapter(list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ====== LOAD POST TỪ FIRESTORE ======
        loadPosts()

        // ====== SỰ KIỆN CHỌN ẢNH ======
        imgPick.setOnClickListener {
            pickImage.launch("image/*")
        }

        // ====== SỰ KIỆN ĐĂNG BÀI ======
        btnPost.setOnClickListener {
            val content = edtPost.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung bài viết", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = currentUser.uid
            val firestore = FirebaseFirestore.getInstance()

            // Lấy thông tin người dùng từ Firestore
            firestore.collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("name")
                        ?: currentUser.email?.substringBefore("@")
                        ?: "user"

                    val hoten = document.getString("hoten") ?: username   // Ưu tiên họ tên

                    // Tạo dữ liệu bài viết
                    val newPost = hashMapOf<String, Any>(
                        "userId" to userId,
                        "name" to username,        // Giữ username để dễ query
                        "hoten" to hoten,          // Lưu họ tên để hiển thị đẹp
                        "content" to content,
                        "likes" to 0,
                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )

                    // Xử lý đăng bài
                    if (imageUri != null) {
                        uploadImageCloudinary(content, imageUri!!, newPost)   // Truyền thêm newPost
                    } else {
                        savePost(newPost)                                     // Không có ảnh
                    }

                    // Reset sau khi đăng
                    edtPost.setText("")
                    imageUri = null
                    imgPick.setImageResource(R.drawable.daucong)
                }
        }

        // ====== NÚT QUAY LẠI ======
        findViewById<ImageView>(R.id.btnBackHome).setOnClickListener {
            finish()
        }
    }

    // ====== UPLOAD ẢNH LÊN CLOUDINARY ======
    fun uploadImageCloudinary(content: String, uri: Uri, postData: HashMap<String, Any>) {
        MediaManager.get().upload(uri)
            .option("folder", "posts")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val imageUrl = resultData?.get("secure_url")?.toString() ?: ""

                    Log.d("Cloudinary", "Upload success: $imageUrl")

                    // Thêm imageUrl vào postData
                    postData["imageUrl"] = imageUrl

                    // Lưu bài viết lên Firestore
                    savePost(postData)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload error: ${error?.description}")
                    savePost(postData)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }



    // ====== LƯU POST VÀO LIST + FIRESTORE ======
    fun savePost(postData: HashMap<String, Any>){

        db.collection("posts")
            .add(postData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                loadPosts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Đăng bài thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SavePost", "Error: ${e.message}")
            }
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    // ====== LOAD POST TỪ FIRESTORE VÀ ADD THÊM VÀO LIST ======
    fun loadPosts() {

        db.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                list.clear()

                for (doc in result) {
                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id
                    list.add(post)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FeedActivity", "Lỗi tải bài viết: ${e.message}")
            }
    }
}