package com.example.demo.activities

import android.app.AlertDialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R

class CommentAdapter(
    private val list: MutableList<Comment>,
    private val db: DatabaseHelper,
    private val reload: () -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtUser: TextView = itemView.findViewById(R.id.txtUser)
        val txtContent: TextView = itemView.findViewById(R.id.txtContent)
        val imgComment: ImageView = itemView.findViewById(R.id.imgComment)
        val btnMenu: ImageButton = itemView.findViewById(R.id.btnMenuComment)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)

        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {

        val comment = list[position]

        holder.txtUser.text = comment.user
        holder.txtContent.text = comment.content

        if (comment.image.isNotEmpty()) {

            holder.imgComment.visibility = View.VISIBLE

            try {
                Glide.with(holder.itemView.context)
                    .load(Uri.parse(comment.image))
                    .into(holder.imgComment)
            } catch (e: Exception) {
                holder.imgComment.visibility = View.GONE
            }

        } else {

            holder.imgComment.visibility = View.GONE
        }

        holder.btnMenu.setOnClickListener {

            val popup = PopupMenu(holder.itemView.context, holder.btnMenu)

            popup.menu.add("Edit")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener {

                if (it.title == "Delete") {

                    val pos = holder.adapterPosition
                    if (pos == RecyclerView.NO_POSITION) return@setOnMenuItemClickListener true

                    db.deleteComment(comment.id)

                    reload()

                }

                if (it.title == "Edit") {

                    val edit = EditText(holder.itemView.context)
                    edit.setText(comment.content)

                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Edit Comment")
                        .setView(edit)
                        .setPositiveButton("Save") { _, _ ->

                            val pos = holder.adapterPosition
                            if (pos == RecyclerView.NO_POSITION) return@setPositiveButton

                            comment.content = edit.text.toString()

                            db.updateComment(comment)

                            reload()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                true
            }

            popup.show()
        }
    }


    override fun getItemCount(): Int = list.size
}