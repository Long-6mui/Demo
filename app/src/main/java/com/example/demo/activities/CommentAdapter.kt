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
            // 1. Lấy thông tin user hiện tại đang đăng nhập
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

            // 2. Kiểm tra quyền sở hữu
            // So sánh UID người dùng hiện tại với userId lưu trong comment
            if (currentUser == null || comment.userId != currentUser.uid) {
                Toast.makeText(
                    holder.itemView.context,
                    "Bạn không có quyền chỉnh sửa hoặc xóa bình luận này",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 3. Nếu đúng là chủ sở hữu mới hiển thị PopupMenu
            val popup = android.widget.PopupMenu(holder.itemView.context, holder.btnMenu)
            popup.menu.add("Edit")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener {
                val db = FirebaseFirestore.getInstance()

                // Xử lý Delete comment
                if (it.title == "Delete") {
                    // Thêm xác nhận xóa để tránh bấm nhầm
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Xóa bình luận")
                        .setMessage("Bạn có chắc chắn muốn xóa bình luận này không?")
                        .setPositiveButton("Xóa") { _, _ ->
                            db.collection("comments").document(comment.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show()
                                    reload()
                                }
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                }

                // Xử lý Edit comment
                if (it.title == "Edit") {
                    // ... (Giữ nguyên logic mở Dialog Edit của bạn ở đây)
                }

                true
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = list.size // số lượng comment
}