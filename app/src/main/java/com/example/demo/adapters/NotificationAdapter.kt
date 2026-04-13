package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.R
import com.example.demo.models.Notification
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NotificationAdapter(private val list: List<Notification>, private val isAdminPage: Boolean = false) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtContent: TextView = view.findViewById(R.id.txtContent)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val noti = list[position]
        val context = holder.itemView.context
        
        // 1. Hiển thị nội dung
        updateNotiText(holder, noti, noti.fromUserName)
        holder.txtTime.text = getRelativeTimeSpan(noti.timestamp)

        // 2. XỬ LÝ AVATAR ĐỒNG BỘ
        when (noti.type) {
            "new_recipe", "admin_reply" -> {
                // ĐỔI AVATAR CHO THÔNG BÁO TỪ ADMIN (Dùng logo app hoặc icon admin)
                holder.imgAvatar.setImageResource(R.drawable.admin) // Thay R.drawable.logo bằng tên file logo của bạn
            }
            else -> {
                // Tải Avatar người dùng cho Like/Comment/Feedback
                if (noti.fromUserId.isNotEmpty()) {
                    FirebaseFirestore.getInstance().collection("Users").document(noti.fromUserId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val hoten = document.getString("hoten") ?: ""
                                val username = document.getString("name") ?: ""
                                val displayName = if (hoten.isNotEmpty()) hoten else if (username.isNotEmpty()) username else noti.fromUserName
                                
                                updateNotiText(holder, noti, displayName)

                                val avatarUrl = document.getString("avatar") ?: ""
                                if (avatarUrl.isNotEmpty()) {
                                    Glide.with(context).load(avatarUrl).circleCrop().placeholder(R.drawable.avtque).into(holder.imgAvatar)
                                } else {
                                    holder.imgAvatar.setImageResource(R.drawable.avtque)
                                }
                            }
                        }
                } else {
                    holder.imgAvatar.setImageResource(R.drawable.avtque)
                }
            }
        }
    }

    private fun updateNotiText(holder: ViewHolder, noti: Notification, name: String) {
        val contentText = when (noti.type) {
            "like" -> "$name đã thích bài viết của bạn."
            "comment" -> {
                val cleanContent = if (noti.content.contains("đã bình luận:", ignoreCase = true)) {
                    noti.content.replace("đã bình luận:", "").trim()
                } else noti.content.trim()
                "$name đã bình luận: $cleanContent"
            }
            "new_recipe" -> "Admin đã thêm công thức mới: ${noti.content}"
            "new_user" -> "Người dùng mới: $name đã đăng ký tài khoản."
            "admin_reply" -> "Admin đã phản hồi góp ý của bạn: ${noti.content}"
            "feedback" -> "$name đã gửi một góp ý mới."
            else -> noti.content
        }
        holder.txtContent.text = contentText
    }

    private fun getRelativeTimeSpan(time: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - time
        return when {
            diff < 60 * 1000 -> "Vừa xong"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} phút trước"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} giờ trước"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} ngày trước"
            else -> {
                val cal = Calendar.getInstance()
                cal.timeInMillis = time
                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
            }
        }
    }

    override fun getItemCount() = list.size
}