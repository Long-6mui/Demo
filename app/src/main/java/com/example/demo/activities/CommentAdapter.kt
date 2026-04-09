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

class CommentAdapter(
    private val list: MutableList<Comment>,
    private val reload: () -> Unit,
    private val pickImageLauncher: ((Uri) -> Unit) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val txtUser: TextView = itemView.findViewById(R.id.txtUser)
        val txtContent: TextView = itemView.findViewById(R.id.txtContent)
        val imgComment: ImageView = itemView.findViewById(R.id.imgComment)
        val btnMenu: ImageButton = itemView.findViewById(R.id.btnMenuComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = list[position]
        holder.txtUser.text = "Đang tải..."
        holder.imgAvatar.setImageResource(R.drawable.avtque)
        holder.txtContent.text = comment.content

        if (comment.image.isNotEmpty()) {
            holder.imgComment.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(comment.image).into(holder.imgComment)
        } else {
            holder.imgComment.visibility = View.GONE
        }

        val firestore = FirebaseFirestore.getInstance()

        if (!comment.userId.isNullOrEmpty()) {
            firestore.collection("Users").document(comment.userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val hoten = document.getString("hoten") ?: ""
                        val name = document.getString("name") ?: ""
                        val avatarUrl = document.getString("avatar") ?: ""
                        holder.txtUser.text = if (hoten.isNotEmpty()) hoten else name
                        if (avatarUrl.isNotEmpty()) {
                            Glide.with(holder.itemView.context).load(avatarUrl).placeholder(R.drawable.avtque).circleCrop().into(holder.imgAvatar)
                        }
                    }
                }
        }

        holder.btnMenu.setOnClickListener {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) return@setOnClickListener

            firestore.collection("Users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
                val role = userDoc.getString("role") ?: "user"
                val isAdmin = role == "admin"
                val isOwner = comment.userId == currentUser.uid

                // Admin HOẶC Chủ bình luận mới được mở Menu
                if (isAdmin || isOwner) {
                    val popup = android.widget.PopupMenu(holder.itemView.context, holder.btnMenu)
                    
                    // CHỈ CHỦ BÌNH LUẬN MỚI ĐƯỢC SỬA (Admin không được sửa bài user)
                    if (isOwner) {
                        popup.menu.add("Edit")
                    }
                    
                    // CẢ ADMIN VÀ CHỦ BÌNH LUẬN ĐỀU ĐƯỢC XÓA
                    popup.menu.add("Delete")

                    popup.setOnMenuItemClickListener {
                        if (it.title == "Delete") {
                            AlertDialog.Builder(holder.itemView.context, R.style.CustomDialogTheme)
                                .setTitle("Xóa bình luận")
                                .setMessage("Bạn có chắc chắn muốn xóa bình luận này không?")
                                .setPositiveButton("Xóa") { _, _ ->
                                    firestore.collection("comments").document(comment.id).delete().addOnSuccessListener { 
                                        Toast.makeText(holder.itemView.context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show()
                                        reload() 
                                    }
                                }
                                .setNegativeButton("Hủy", null).show()
                        }

                        if (it.title == "Edit") {
                            val context = holder.itemView.context
                            val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_edit_comment, null)
                            val edtContent = dialogView.findViewById<EditText>(R.id.edtEditComment)
                            val imgCommentView = dialogView.findViewById<ImageView>(R.id.imgEditComment)
                            val btnPickImage = dialogView.findViewById<ImageView>(R.id.btnPickImageDialog)

                            edtContent.setText(comment.content)
                            var selectedImageUri: Uri? = null

                            if (comment.image.isNotEmpty()) {
                                imgCommentView.visibility = View.VISIBLE
                                Glide.with(context).load(comment.image).into(imgCommentView)
                            }

                            btnPickImage.setOnClickListener {
                                pickImageLauncher { uri ->
                                    selectedImageUri = uri
                                    imgCommentView.setImageURI(uri)
                                    imgCommentView.visibility = View.VISIBLE
                                }
                            }

                            AlertDialog.Builder(context, R.style.CustomDialogTheme)
                                .setTitle("Edit Comment")
                                .setView(dialogView)
                                .setPositiveButton("Save") { _, _ ->
                                    val newContent = edtContent.text.toString().trim()
                                    if (selectedImageUri != null) {
                                        MediaManager.get().upload(selectedImageUri).option("folder", "comments")
                                            .callback(object : UploadCallback {
                                                override fun onStart(requestId: String?) {}
                                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                                                    val newUrl = resultData?.get("secure_url")?.toString() ?: ""
                                                    firestore.collection("comments").document(comment.id)
                                                        .update(mapOf("content" to newContent, "image" to newUrl))
                                                        .addOnSuccessListener { reload() }
                                                }
                                                override fun onError(requestId: String?, error: ErrorInfo?) {}
                                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                                            }).dispatch()
                                    } else {
                                        firestore.collection("comments").document(comment.id)
                                            .update("content", newContent).addOnSuccessListener { reload() }
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                        true
                    }
                    popup.show()
                } else {
                    Toast.makeText(holder.itemView.context, "Bạn không có quyền thực hiện thao tác này", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size
}