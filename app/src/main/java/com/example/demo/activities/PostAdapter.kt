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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlin.jvm.java
import com.example.demo.R
import com.google.firebase.firestore.FirebaseFirestore


class PostAdapter(private val list: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_post, parent, false)

        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

        val post = list[position]
        holder.imgAvatar.setImageResource(post.imgAvatar)
        holder.name.text = post.name
        holder.content.text = post.content
        holder.likes.text = "${post.likes} likes"


        if (!post.imageUrl.isNullOrEmpty()) {

            holder.image.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.image)

        } else if (post.image != null) {

            holder.image.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(post.image)
                .into(holder.image)

        } else {

            holder.image.visibility = View.GONE

        }

        holder.likeBtn.setOnClickListener {
            post.likes++
            notifyItemChanged(position)
        }



        holder.commentBtn.setOnClickListener {

            Toast.makeText(
                holder.itemView.context,
                "Click comment",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(holder.itemView.context, CommentActivity::class.java)
            intent.putExtra("postId", post.id)

            holder.itemView.context.startActivity(intent)
        }

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

                if(it.title == "Delete"){

                    val db = FirebaseFirestore.getInstance()

                    db.collection("posts")
                        .document(post.id)
                        .delete()
                        .addOnSuccessListener {

                            val pos = holder.adapterPosition

                            list.removeAt(pos)
                            notifyItemRemoved(pos)

                        }

                }

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