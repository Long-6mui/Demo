package com.example.demo.activities

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Log.e
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.demo.Database.DatabaseHelper
import kotlin.jvm.java
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class PostAdapter(private val list: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // ViewHolder giữ các view của 1 item post
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)

        val name: TextView = itemView.findViewById(R.id.txtName)
        val content: TextView = itemView.findViewById(R.id.txtContent)
        val likes: TextView = itemView.findViewById(R.id.txtLikes)
        val image: ImageView = itemView.findViewById(R.id.image)
        val likeBtn: Button = itemView.findViewById(R.id.btnLike)
        val commentBtn: Button = itemView.findViewById(R.id.btnComment)

        val btnMenu: ImageButton = itemView.findViewById(R.id.btnMenu)




    }

    // Tạo view cho mỗi post
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_post, parent, false)

        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = list[position]
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid ?: ""

        // --- 1. XỬ LÝ HIỂN THỊ USER (GIỮ NGUYÊN) ---
        holder.name.text = "Đang tải..."
        holder.imgAvatar.setImageResource(R.drawable.avtque)

        if (!post.userId.isNullOrEmpty()) {
            firestore.collection("Users").document(post.userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val hoten = document.getString("hoten") ?: ""
                        val name = document.getString("name") ?: ""
                        val avatarUrl = document.getString("avatar") ?: ""
                        holder.name.text = if (hoten.isNotEmpty()) hoten else name
                        if (avatarUrl.isNotEmpty()) {
                            Glide.with(holder.itemView.context).load(avatarUrl)
                                .placeholder(R.drawable.avtque).error(R.drawable.avtque).into(holder.imgAvatar)
                        }
                    } else {
                        holder.name.text = post.name ?: "Unknown User"
                    }
                }
        }

        // --- 2. XỬ LÝ NỘI DUNG & ẢNH (GIỮ NGUYÊN) ---
        holder.content.text = post.content
        if (!post.imageUrl.isNullOrEmpty()) {
            holder.image.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(post.imageUrl).into(holder.image)
        } else if (post.image != null) {
            holder.image.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(post.image).into(holder.image)
        } else {
            holder.image.visibility = View.GONE
        }

        // --- 3. XỬ LÝ LIKE (PHẦN CẬP NHẬT MỚI) ---
        // Hiển thị số lượng like từ danh sách likedBy
        val likeCount = post.likedBy.size
        holder.likes.text = "$likeCount likes"

        // Kiểm tra xem User hiện tại đã like chưa
        val isLiked = post.likedBy.contains(currentUserId)
        if (isLiked) {
            // Trạng thái ĐÃ LIKE
            holder.likeBtn.text = "UnLike"


        } else {
            // Trạng thái CHƯA LIKE
            holder.likeBtn.text = "Like"

        }
        holder.likeBtn.setOnClickListener {
            if (currentUserId.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val postRef = firestore.collection("posts").document(post.id)

            if (isLiked) {
                // Nếu đã like -> Bỏ like (Xóa UID khỏi mảng)
                post.likedBy.remove(currentUserId)
                postRef.update("likedBy", FieldValue.arrayRemove(currentUserId))
            } else {
                // Nếu chưa like -> Thêm like (Thêm UID vào mảng)
                post.likedBy.add(currentUserId)
                postRef.update("likedBy", FieldValue.arrayUnion(currentUserId))
            }
            // Cập nhật giao diện ngay lập tức tại vị trí này
            notifyItemChanged(position)
        }

        // --- 4. COMMENT (GIỮ NGUYÊN) ---
        holder.commentBtn.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("postId", post.id)
            context.startActivity(intent)
        }

        // --- 5. MENU EDIT/DELETE (GIỮ NGUYÊN) ---
        holder.btnMenu.setOnClickListener {
            if (post.id.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Bài viết mẫu không thể chỉnh sửa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUser == null || post.userId != currentUser.uid) {
                Toast.makeText(holder.itemView.context, "Bạn không có quyền chỉnh sửa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val popup = PopupMenu(holder.itemView.context, holder.btnMenu)
            popup.menu.add("Edit")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Delete" -> {
                        firestore.collection("posts").document(post.id).delete()
                            .addOnSuccessListener {
                                val pos = holder.adapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    list.removeAt(pos)
                                    notifyItemRemoved(pos)
                                    Toast.makeText(holder.itemView.context, "Đã xóa", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                    "Edit" -> {
                        val intent = Intent(holder.itemView.context, EditPostActivity::class.java)
                        intent.putExtra("postId", post.id)
                        intent.putExtra("content", post.content)
                        intent.putExtra("image", post.imageUrl)
                        intent.putExtra("position", position)
                        holder.itemView.context.startActivity(intent)
                    }
                }
                true
            }
            popup.show()
        }

    }

    override fun getItemCount(): Int = list.size
}