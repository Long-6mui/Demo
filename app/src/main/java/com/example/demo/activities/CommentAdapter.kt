package com.example.demo.activities

import android.app.AlertDialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.demo.R
import com.google.firebase.firestore.FirebaseFirestore

// Adapter hiển thị danh sách comment
class CommentAdapter(
    private val list: MutableList<Comment>,           // danh sách comment
    private val reload: () -> Unit,                   // callback để reload comment từ Activity
    private val pickImageLauncher: ((Uri) -> Unit) -> Unit // callback để chọn ảnh khi edit comment
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    // ViewHolder cho mỗi item comment
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar) // avatar user
        val txtUser: TextView = itemView.findViewById(R.id.txtUser)      // tên user
        val txtContent: TextView = itemView.findViewById(R.id.txtContent) // nội dung comment
        val imgComment: ImageView = itemView.findViewById(R.id.imgComment) // ảnh comment
        val btnMenu: ImageButton = itemView.findViewById(R.id.btnMenuComment) // menu edit/delete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false) // inflate layout item comment
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = list[position]

        // Hiển thị thông tin comment
        holder.txtUser.text = "Đang tải..."
        holder.imgAvatar.setImageResource(R.drawable.avtque)
        holder.txtContent.text = comment.content

        // Nếu comment có ảnh → hiển thị ảnh
        if (comment.image.isNotEmpty()) {
            holder.imgComment.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(comment.image).into(holder.imgComment)
        } else {
            holder.imgComment.visibility = View.GONE
        }

        val firestore = FirebaseFirestore.getInstance()

        if (!comment.userId.isNullOrEmpty()) {
            firestore.collection("Users")
                .document(comment.userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val hoten = document.getString("hoten") ?: ""
                        val name = document.getString("name") ?: ""
                        val avatarUrl = document.getString("avatar") ?: ""

                        // Ưu tiên hiển thị họ tên (giống trang Post)
                        holder.txtUser.text = if (hoten.isNotEmpty()) hoten else name

                        // Load avatar
                        if (avatarUrl.isNotEmpty()) {
                            Glide.with(holder.itemView.context)
                                .load(avatarUrl)
                                .placeholder(R.drawable.avtque)
                                .error(R.drawable.avtque)
                                .circleCrop()
                                .into(holder.imgAvatar)
                        } else {
                            holder.imgAvatar.setImageResource(R.drawable.avtque)
                        }
                    } else {
                        // Fallback
                        holder.txtUser.text = comment.userId.take(8) + "..." // rút gọn UID nếu không tìm thấy
                    }
                }
                .addOnFailureListener {
                    holder.txtUser.text = "Unknown User"
                    holder.imgAvatar.setImageResource(R.drawable.avtque)
                }
        } else {
            holder.txtUser.text = "Unknown User"
        }

        // Nút menu (Edit/Delete)
        holder.btnMenu.setOnClickListener {
            val popup = android.widget.PopupMenu(holder.itemView.context, holder.btnMenu)
            popup.menu.add("Edit")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener {
                val db = FirebaseFirestore.getInstance()

                // Xử lý Delete comment
                if (it.title == "Delete") {
                    db.collection("comments").document(comment.id)
                        .delete()
                        .addOnSuccessListener { reload() } // reload danh sách sau khi xóa
                }

                // Xử lý Edit comment
                if (it.title == "Edit") {
                    val context = holder.itemView.context
                    val dialogView = LayoutInflater.from(context)
                        .inflate(R.layout.activity_edit_comment, null) // layout edit comment
                    val edtContent = dialogView.findViewById<EditText>(R.id.edtEditComment)
                    val imgCommentView = dialogView.findViewById<ImageView>(R.id.imgEditComment)
                    val btnPickImage = dialogView.findViewById<ImageView>(R.id.btnPickImageDialog)

                    edtContent.setText(comment.content) // hiển thị nội dung cũ
                    var selectedImageUri: Uri? = null // URI ảnh mới nếu chọn

                    // Nếu comment đã có ảnh → hiển thị
                    if (comment.image.isNotEmpty()) {
                        imgCommentView.visibility = View.VISIBLE
                        Glide.with(context).load(comment.image).into(imgCommentView)
                    } else {
                        imgCommentView.visibility = View.GONE
                    }

                    // Chọn ảnh mới → callback sang Activity
                    btnPickImage.setOnClickListener {
                        pickImageLauncher { uri ->
                            selectedImageUri = uri
                            imgCommentView.setImageURI(uri) // hiển thị ảnh mới
                            imgCommentView.visibility = View.VISIBLE
                        }
                    }

                    // Hiển thị dialog edit comment
                    AlertDialog.Builder(context)
                        .setTitle("Edit Comment")
                        .setView(dialogView)
                        .setPositiveButton("Save") { _, _ ->
                            val newContent = edtContent.text.toString().trim()
                            if (newContent.isEmpty() && selectedImageUri == null) return@setPositiveButton

                            // Nếu chọn ảnh mới → upload Cloudinary trước rồi update Firestore
                            if (selectedImageUri != null) {
                                MediaManager.get().upload(selectedImageUri)
                                    .option("folder", "comments")
                                    .callback(object : UploadCallback {
                                        override fun onStart(requestId: String?) {}
                                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                        override fun onSuccess(
                                            requestId: String?,
                                            resultData: MutableMap<Any?, Any?>?
                                        ) {
                                            val newUrl = resultData?.get("secure_url")?.toString() ?: ""
                                            db.collection("comments").document(comment.id)
                                                .update(mapOf("content" to newContent, "image" to newUrl))
                                                .addOnSuccessListener { reload() } // reload comment
                                        }
                                        override fun onError(requestId: String?, error: ErrorInfo?) {
                                            Toast.makeText(context, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show()
                                        }
                                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                                    }).dispatch()
                            } else {
                                // Chỉ update nội dung comment
                                db.collection("comments").document(comment.id)
                                    .update("content", newContent)
                                    .addOnSuccessListener { reload() } // reload comment
                            }
                        }
                        .setNegativeButton("Cancel", null) // hủy edit
                        .show()
                }

                true
            }
            popup.show() // hiển thị popup menu
        }
    }

    override fun getItemCount(): Int = list.size // số lượng comment
}