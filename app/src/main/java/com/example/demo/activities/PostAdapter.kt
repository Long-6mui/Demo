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

        // Reset trước khi load
        holder.name.text = "Đang tải..."
        holder.imgAvatar.setImageResource(R.drawable.avtque)

        // Hiển thị avatar, tên, nội dung, số like
        val firestore = FirebaseFirestore.getInstance()

        if (!post.userId.isNullOrEmpty()) {
            firestore.collection("Users")
                .document(post.userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val hoten = document.getString("hoten") ?: ""
                        val name = document.getString("name") ?: ""           // username
                        val avatarUrl = document.getString("avatar") ?: ""

                        // Hiển thị tên (ưu tiên hoten, nếu rỗng thì lấy name)
                        holder.name.text = if (hoten.isNotEmpty()) hoten else name

                        // Hiển thị avatar
                        if (avatarUrl.isNotEmpty()) {
                            Glide.with(holder.itemView.context)
                                .load(avatarUrl)
                                .placeholder(R.drawable.avtque)
                                .error(R.drawable.avtque)
                                .into(holder.imgAvatar)
                        } else {
                            holder.imgAvatar.setImageResource(R.drawable.avtque)
                        }
                    } else {
                        // Fallback nếu không tìm thấy user trong Firestore
                        holder.name.text = post.name ?: "Unknown User"
                    }
                }
                .addOnFailureListener {
                    holder.name.text = post.name ?: "Unknown User"
                    holder.imgAvatar.setImageResource(R.drawable.avtque)
                }
        }
        else if (!post.name.isNullOrEmpty()) {
            firestore.collection("Users")
                .whereEqualTo("name", post.name)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val document = querySnapshot.documents.firstOrNull()

                    if (document != null) {
                        val hoten = document.getString("hoten") ?: ""
                        val username = document.getString("name") ?: ""
                        val avatarUrl = document.getString("avatar") ?: ""

                        holder.name.text = if (hoten.isNotEmpty()) hoten else username

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
                        holder.name.text = post.name
                    }
                }
                .addOnFailureListener {
                    holder.name.text = post.name ?: "Unknown User"
                    holder.imgAvatar.setImageResource(R.drawable.avtque)
                }
        }


        holder.content.text = post.content
        holder.likes.text = "${post.likes} likes"

        // Hiển thị ảnh post
        if (!post.imageUrl.isNullOrEmpty()) {

            holder.image.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.image)

        } else if (post.image != null) {

            // Ảnh mẫu trong drawable
            holder.image.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(post.image)
                .into(holder.image)

        } else {

            holder.image.visibility = View.GONE

        }

        //Like
        holder.likeBtn.setOnClickListener {
            post.likes++
            notifyItemChanged(position)
        }



        //Comment
        holder.commentBtn.setOnClickListener {

            Toast.makeText(
                holder.itemView.context,
                "Click comment",
                Toast.LENGTH_SHORT
            ).show()

            val context = holder.itemView.context
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("postId", post.id)  // post.id là id của bài viết hiện tại
            context.startActivity(intent)
        }

        //Menu Edit/Delete
        holder.btnMenu.setOnClickListener {

            // nếu là bài mẫu thì không cho chỉnh sửa
            if(post.id.isEmpty()){
                Toast.makeText(
                    holder.itemView.context,
                    "Chỉ chỉnh sửa được bài bạn đăng",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val popup = PopupMenu(holder.itemView.context, holder.btnMenu)

            popup.menu.add("Edit")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener {

                //Delete post
                if(it.title == "Delete"){

                    val db = FirebaseFirestore.getInstance()

                    db.collection("posts")
                        .document(post.id)
                        .delete()
                        .addOnSuccessListener {

                            val pos = holder.adapterPosition

                            list.removeAt(pos)  // xóa khỏi list
                            notifyItemRemoved(pos) // cập nhật RecyclerView

                        }

                }

                //Edit Post
                if(it.title == "Edit"){

                    val intent = Intent(holder.itemView.context, EditPostActivity::class.java)

                    intent.putExtra("postId", post.id)
                    intent.putExtra("content", post.content)
                    intent.putExtra("image", post.imageUrl)
                    intent.putExtra("position", position)

                    holder.itemView.context.startActivity(intent)

                }

                true
            }

            popup.show()
        }

    }

    override fun getItemCount(): Int = list.size
}