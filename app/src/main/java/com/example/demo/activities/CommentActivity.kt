package com.example.demo.activities

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {

    // UI components
    private lateinit var edtComment: EditText // ô nhập nội dung comment
    private lateinit var btnSend: ImageView   // nút gửi comment
    private lateinit var btnPickImage: ImageView // nút chọn ảnh comment
    private lateinit var recyclerView: RecyclerView // danh sách comment

    // Firebase
    private lateinit var auth: FirebaseAuth // quản lý user đăng nhập
    private val db = FirebaseFirestore.getInstance() // Firestore
    private var postId: String = "" // id bài viết hiện tại, để load comment đúng bài

    // Xử lý ảnh
    private var newCommentImageUri: Uri? = null // lưu URI ảnh comment mới
    private var editCommentCallback: ((Uri) -> Unit)? = null // callback khi edit ảnh comment

    // Data comment
    private val list = mutableListOf<Comment>() // danh sách comment
    private lateinit var adapter: CommentAdapter // adapter cho RecyclerView

    // Launcher chọn ảnh cho comment mới
    private val pickNewCommentImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                newCommentImageUri = it // lưu URI ảnh
                btnPickImage.setImageURI(it) // hiển thị ảnh đã chọn lên nút
            }
        }

    // Launcher chọn ảnh khi edit comment
    private val pickEditCommentImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { editCommentCallback?.invoke(it) } // trả ảnh về callback của adapter
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        // Liên kết UI
        edtComment = findViewById(R.id.edtComment)
        btnSend = findViewById(R.id.btnSend)
        btnPickImage = findViewById(R.id.btnPickImage)
        recyclerView = findViewById(R.id.recyclerComment)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()
        postId = intent.getStringExtra("postId") ?: "" // lấy postId từ intent

        // Adapter, callback edit ảnh
        adapter = CommentAdapter(list, { loadComments() }) { callback ->
            editCommentCallback = callback // lưu callback để edit ảnh comment
            pickEditCommentImageLauncher.launch("image/*") // mở chọn ảnh
        }

        // Thiết lập RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this) // danh sách dọc
        recyclerView.adapter = adapter

        loadComments() // load comment ban đầu cho bài viết này

        // Chọn ảnh cho comment mới
        btnPickImage.setOnClickListener { pickNewCommentImageLauncher.launch("image/*") }

        // Gửi comment mới
        btnSend.setOnClickListener {
            val content = edtComment.text.toString().trim() // lấy nội dung comment
            if (content.isEmpty() && newCommentImageUri == null) return@setOnClickListener
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            if (newCommentImageUri != null) {
                // Nếu có ảnh → upload lên Cloudinary rồi save comment
                uploadImageToCloudinary(newCommentImageUri!!) { url ->
                    saveComment(content, url, uid)
                }
            } else {
                // Nếu không có ảnh → save trực tiếp
                saveComment(content, "", uid)
            }

            // Reset ô nhập và nút ảnh
            edtComment.setText("")
            newCommentImageUri = null
            btnPickImage.setImageResource(R.drawable.daucong)
        }

        // Nút quay lại
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    // Load tất cả comment của bài viết này từ Firestore
    private fun loadComments() {
        db.collection("comments")
            .whereEqualTo("postId", postId) // chỉ load comment của bài này
            .get()
            .addOnSuccessListener { result ->
                list.clear() // xóa comment cũ
                for (doc in result) {
                    val comment = doc.toObject(Comment::class.java) // convert document thành object
                    comment.id = doc.id // lưu id document
                    list.add(comment)
                }
                adapter.notifyDataSetChanged() // cập nhật RecyclerView
            }
    }

    // Lưu comment vào Firestore
    private fun saveComment(content: String, imageUrl: String, uid: String) {
        val data = hashMapOf(
            "postId" to postId,
            "userId" to uid,
            "content" to content,
            "image" to imageUrl
        )
        db.collection("comments").add(data)
            .addOnSuccessListener { loadComments() }  // reload comment đúng bài
    }

    // Upload ảnh lên Cloudinary
    private fun uploadImageToCloudinary(uri: Uri, onSuccess: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .option("folder", "comments") // lưu trong folder "comments"
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {} // bắt đầu upload
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {} // tiến trình upload
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url")?.toString() ?: "" // lấy URL ảnh
                    onSuccess(url) // trả về callback
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(this@CommentActivity, "Upload thất bại", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {} // nếu upload bị reschedule
            }).dispatch()
    }
}