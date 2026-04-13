package com.example.demo.adapters

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.R
import com.example.demo.activities.CommentActivity
import com.example.demo.activities.EditPostActivity
import com.example.demo.models.Post
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private val list: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val name: TextView = itemView.findViewById(R.id.txtName)
        val content: TextView = itemView.findViewById(R.id.txtContent)
        val likes: TextView = itemView.findViewById(R.id.txtLikes)
        val image: ImageView = itemView.findViewById(R.id.image)
        val likeBtn: MaterialButton = itemView.findViewById(R.id.btnLike)
        val commentBtn: Button = itemView.findViewById(R.id.btnComment)
        val btnMenu: ImageButton = itemView.findViewById(R.id.btnMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = list[position]
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid ?: ""

        holder.imgAvatar.setImageResource(R.drawable.avtque)

        // Load User Info
        if (!post.userId.isNullOrEmpty()) {
            firestore.collection("Users").document(post.userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val hoten = document.getString("hoten") ?: ""
                        val name = document.getString("name") ?: ""
                        holder.name.text = if (hoten.isNotEmpty()) hoten else name
                        val avatarUrl = document.getString("avatar") ?: ""
                        if (avatarUrl.isNotEmpty()) {
                            Glide.with(holder.itemView.context)
                                .load(avatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.avtque)
                                .error(R.drawable.avtque)
                                .into(holder.imgAvatar)
                        }else {
                            holder.imgAvatar.setImageResource(R.drawable.avtque)
                        }
                    }else {
                        holder.name.text = "Người dùng ẩn danh"
                    }
                }
                .addOnFailureListener {
                    holder.name.text = "Lỗi tải thông tin"
                }
        }

        holder.content.text = post.content
        if (!post.imageUrl.isNullOrEmpty()) {
            holder.image.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(post.imageUrl).into(holder.image)
        } else {
            holder.image.visibility = View.GONE
        }

        val likeCount = post.likedBy.size
        holder.likes.text = "$likeCount likes"

        val isLiked = post.likedBy.contains(currentUserId)
        if (isLiked) {
            holder.likeBtn.setIconResource(R.drawable.ic_heart_filled)
            holder.likeBtn.setIconTint(ColorStateList.valueOf(Color.parseColor("#FF4B4B")))
        } else {
            holder.likeBtn.setIconResource(R.drawable.ic_heart_outline)
            holder.likeBtn.setIconTint(null)
        }

        holder.likeBtn.setOnClickListener {
            if (currentUserId.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val postRef = firestore.collection("posts").document(post.id)

            if (isLiked) {
                post.likedBy.remove(currentUserId)
                postRef.update("likedBy", FieldValue.arrayRemove(currentUserId))
            } else {
                post.likedBy.add(currentUserId)
                postRef.update("likedBy", FieldValue.arrayUnion(currentUserId))
                
                // LOGIC THÔNG BÁO THÔNG MINH
                if (post.userId != currentUserId) {
                    firestore.collection("Users").document(currentUserId).get().addOnSuccessListener { userDoc ->
                        val hoten = userDoc.getString("hoten") ?: ""
                        val name = userDoc.getString("name") ?: ""
                        val fromName = if (hoten.isNotEmpty()) hoten else name
                        val role = userDoc.getString("role") ?: "user"
                        val timestamp = System.currentTimeMillis()

                        // 1. Thông báo cá nhân cho chủ bài viết (User)
                        val userNoti = hashMapOf(
                            "fromUserId" to currentUserId,
                            "fromUserName" to fromName,
                            "toUserId" to post.userId,
                            "postId" to post.id,
                            "type" to "like",
                            "timestamp" to System.currentTimeMillis(),
                            "seen" to false
                        )
                        firestore.collection("notifications").add(userNoti)

                        // 2. CHỈ GỬI CHO ADMIN NẾU BÀI VIẾT ĐÓ LÀ CỦA ADMIN VÀ NGƯỜI LIKE KHÔNG PHẢI ADMIN
                        if (role != "admin") {
                            firestore.collection("Users").document(post.userId).get().addOnSuccessListener { ownerDoc ->
                                if (ownerDoc.getString("role") == "admin") {
                                    val adminNoti = hashMapOf(
                                        "fromUserId" to currentUserId,
                                        "fromUserName" to fromName,
                                        "type" to "like",
                                        "content" to "đã thích bài viết của bạn",
                                        "timestamp" to timestamp,
                                        "seen" to false
                                    )
                                    firestore.collection("admin_notifications").add(adminNoti)
                                }
                            }
                        }
                    }
                }
            }
            notifyItemChanged(position)
        }

        holder.commentBtn.setOnClickListener {
            val intent = Intent(holder.itemView.context, CommentActivity::class.java)
            intent.putExtra("postId", post.id)
            intent.putExtra("postOwnerId", post.userId)
            holder.itemView.context.startActivity(intent)
        }

        holder.btnMenu.setOnClickListener {
            if (currentUser == null) return@setOnClickListener
            firestore.collection("Users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
                val role = userDoc.getString("role") ?: "user"
                val isOwner = post.userId == currentUser.uid
                if (role == "admin" || isOwner) {
                    val popup = PopupMenu(holder.itemView.context, holder.btnMenu)
                    if (isOwner) popup.menu.add("Edit")
                    popup.menu.add("Delete")
                    popup.setOnMenuItemClickListener { menuItem ->
                        if (menuItem.title == "Delete") firestore.collection("posts").document(post.id).delete()
                        true
                    }
                    popup.show()
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updatePosts(newPosts: List<Post>) {
        list.clear()
        list.addAll(newPosts)
        notifyDataSetChanged()
    }
}