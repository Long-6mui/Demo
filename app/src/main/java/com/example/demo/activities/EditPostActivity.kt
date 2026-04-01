package com.example.demo.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.demo.R
import com.google.firebase.firestore.FirebaseFirestore

// Activity chỉnh sửa bài viết
class EditPostActivity : AppCompatActivity() {

    private lateinit var edtContent: EditText   // ô nhập nội dung bài viết
    private lateinit var imgPost: ImageView     // hiển thị ảnh bài viết
    private lateinit var btnSave: Button        // nút lưu bài viết

    private var imageRes: String = ""           // URI cục bộ hoặc URL Cloudinary
    private var postId: String? = null          // id bài viết cần edit
    private var position: Int = -1              // vị trí bài viết trong FeedActivity (dùng để cập nhật UI ngay)

    private val db = FirebaseFirestore.getInstance() // Firestore

    // ActivityResultContract chọn ảnh mới
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageRes = uri.toString() // Lưu URI cục bộ
                imgPost.visibility = View.VISIBLE
                imgPost.setImageURI(uri) // hiển thị ảnh mới lên ImageView
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        // Liên kết UI
        edtContent = findViewById(R.id.edtEditPost)
        imgPost = findViewById(R.id.imgEditPost)
        btnSave = findViewById(R.id.btnSaveEdit)

        findViewById<ImageView>(R.id.btnBackEditPost).setOnClickListener {
            finish() // đóng activity, quay về trang trước
        }

        // Lấy dữ liệu bài viết từ intent
        postId = intent.getStringExtra("postId")
        val content = intent.getStringExtra("content") ?: ""
        imageRes = intent.getStringExtra("image") ?: ""
        position = intent.getIntExtra("position", -1)

        edtContent.setText(content) // hiển thị nội dung cũ

        // Hiển thị ảnh cũ (nếu có)
        if (imageRes.isNotEmpty()) {
            imgPost.visibility = View.VISIBLE
            if (imageRes.startsWith("http")) {
                // Nếu là URL Cloudinary → dùng Glide load ảnh
                Glide.with(this)
                    .load(imageRes)
                    .into(imgPost)
            } else {
                // Nếu là URI cục bộ → hiển thị trực tiếp
                imgPost.setImageURI(Uri.parse(imageRes))
            }
        } else {
            imgPost.visibility = View.GONE
        }

        // Chọn ảnh mới khi click vào ImageView
        imgPost.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Nút Save
        btnSave.setOnClickListener {
            val newContent = edtContent.text.toString().trim()
            if (newContent.isEmpty()) {
                Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageRes.startsWith("content://")) {
                // Nếu chọn ảnh mới → upload lên Cloudinary trước
                uploadImageToCloudinary(Uri.parse(imageRes)) { url ->
                    updatePost(newContent, url)
                }
            } else {
                // Không đổi ảnh hoặc đã là URL Cloudinary → update trực tiếp
                updatePost(newContent, imageRes)
            }
        }
    }

    // Cập nhật bài viết trong Firestore và FeedActivity
    private fun updatePost(content: String, imageUrl: String) {
        val data = hashMapOf(
            "content" to content,  // nội dung mới
            "imageUrl" to imageUrl // ảnh mới hoặc giữ nguyên
        )

        postId?.let {
            db.collection("posts")
                .document(it)
                .update(data as Map<String, Any>)
                .addOnSuccessListener {
                    // Cập nhật trực tiếp FeedActivity nếu position hợp lệ
                    if (position >= 0 && position < FeedActivity.list.size) {
                        FeedActivity.list[position].content = content
                        FeedActivity.list[position].imageUrl = imageUrl
                        FeedActivity.adapter.notifyItemChanged(position)
                    }
                    finish() // đóng EditPostActivity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                    Log.e("EditPost", "Update failed: ${it.message}")
                }
        }
    }

    // Upload ảnh lên Cloudinary
    private fun uploadImageToCloudinary(uri: Uri, onSuccess: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .option("folder", "posts") // lưu trong folder "posts"
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url")?.toString() ?: ""
                    Log.d("Cloudinary", "Upload success: $url")
                    onSuccess(url) // trả về URL để update bài viết
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(this@EditPostActivity, "Upload thất bại", Toast.LENGTH_SHORT).show()
                    Log.e("Cloudinary", "Upload error: ${error?.description}")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}