package com.example.demo.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import kotlin.jvm.java
import com.example.demo.R
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
        holder.image.setImageResource(post.image)

        holder.likeBtn.setOnClickListener {
            post.likes++
            notifyItemChanged(position)
        }

        holder.commentBtn.setOnClickListener {

            val intent = Intent(holder.itemView.context, CommentActivity::class.java)
            holder.itemView.context.startActivity(intent)

        }


    }

    override fun getItemCount(): Int = list.size
}