package com.example.demo.activities

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.demo.R
import com.example.demo.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : BaseActivity() {

    private lateinit var edtComment: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnPickImage: ImageView
    private lateinit var recyclerView: RecyclerView

    private var postId: String = ""
    private var postOwnerId: String = ""

    private var newCommentImageUri: Uri? = null
    private var editCommentCallback: ((Uri) -> Unit)? = null

    private val list = mutableListOf<Comment>()
    private lateinit var adapter: CommentAdapter

    private val pickNewCommentImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                newCommentImageUri = it
                btnPickImage.setImageURI(it)
            }
        }

    private val pickEditCommentImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { editCommentCallback?.invoke(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        edtComment = findViewById(R.id.edtComment)
        btnSend = findViewById(R.id.btnSend)
        btnPickImage = findViewById(R.id.btnPickImage)
        recyclerView = findViewById(R.id.recyclerComment)

        postId = intent.getStringExtra("postId") ?: ""
        postOwnerId = intent.getStringExtra("postOwnerId") ?: ""

        adapter = CommentAdapter(list, { loadComments() }) { callback ->
            editCommentCallback = callback
            pickEditCommentImageLauncher.launch("image/*")
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadComments()

        btnPickImage.setOnClickListener { pickNewCommentImageLauncher.launch("image/*") }

        btnSend.setOnClickListener {
            val content = edtComment.text.toString().trim()
            if (content.isEmpty() && newCommentImageUri == null) return@setOnClickListener
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            if (newCommentImageUri != null) {
                uploadImageToCloudinary(newCommentImageUri!!) { url ->
                    saveComment(content, url, uid)
                }
            } else {
                saveComment(content, "", uid)
            }

            edtComment.setText("")
            newCommentImageUri = null
            btnPickImage.setImageResource(R.drawable.ic_image)
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadComments() {
        db.collection("comments")
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener { result ->
                list.clear()
                for (doc in result) {
                    val comment = doc.toObject(Comment::class.java)
                    comment.id = doc.id
                    list.add(comment)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun saveComment(content: String, imageUrl: String, uid: String) {
        val data = hashMapOf(
            "postId" to postId,
            "userId" to uid,
            "content" to content,
            "image" to imageUrl
        )
        db.collection("comments").add(data)
            .addOnSuccessListener {
                loadComments()
                
                db.collection("Users").document(uid).get().addOnSuccessListener { userDoc ->
                    val hoten = userDoc.getString("hoten") ?: ""
                    val name = userDoc.getString("name") ?: ""
                    val fromName = if (hoten.isNotEmpty()) hoten else name
                    val role = userDoc.getString("role") ?: "user"
                    val timestamp = System.currentTimeMillis()

                    // 1. THÔNG BÁO CHO CHỦ BÀI VIẾT (User) - Chỉ gửi nếu người comment khác chủ bài viết
                    if (uid != postOwnerId && postOwnerId.isNotEmpty()) {
                        val userNoti = hashMapOf(
                            "fromUserId" to uid,
                            "fromUserName" to fromName,
                            "toUserId" to postOwnerId,
                            "postId" to postId,
                            "type" to "comment",
                            "content" to content,
                            "timestamp" to timestamp,
                            "seen" to false
                        )
                        db.collection("notifications").add(userNoti)
                    }

                    // 2. THÔNG BÁO CHO HỆ THỐNG (Admin) - Chỉ gửi nếu chủ bài viết là Admin và người comment không phải Admin
                    if (role != "admin" && postOwnerId.isNotEmpty()) {
                        db.collection("Users").document(postOwnerId).get().addOnSuccessListener { ownerDoc ->
                            if (ownerDoc.getString("role") == "admin") {
                                val adminNoti = hashMapOf(
                                    "fromUserId" to uid,
                                    "fromUserName" to fromName,
                                    "type" to "comment",
                                    "content" to content,
                                    "timestamp" to timestamp,
                                    "seen" to false
                                )
                                db.collection("admin_notifications").add(adminNoti)
                            }
                        }
                    }
                }
            }
    }

    private fun uploadImageToCloudinary(uri: Uri, onSuccess: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .option("folder", "comments")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url")?.toString() ?: ""
                    onSuccess(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(this@CommentActivity, "Upload thất bại", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}