package com.example.demo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R

data class UserAdmin(
    val id: String = "",
    val nameUser: String = "",
    val email: String = "",
    val status: String = "Active" // Active hoặc Blocked
)

class UserAdapter(
    private var userList: MutableList<UserAdmin>,
    private val onBlockClick: (UserAdmin, Int) -> Unit,
    private val onDeleteClick: (UserAdmin, Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtNameUser)
        val txtEmail: TextView = view.findViewById(R.id.txtEmailUser)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val btnBlock: TextView = view.findViewById(R.id.btnBlock)
        val btnDelete: TextView = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.txtName.text = user.nameUser
        holder.txtEmail.text = user.email
        holder.txtStatus.text = if (user.status == "Active") "🟢 Hoạt động" else "🔴 Đã khóa"
        holder.txtStatus.setTextColor(if (user.status == "Active") Color.GREEN else Color.RED)
        
        holder.btnBlock.text = if (user.status == "Active") "Khóa" else "Mở khóa"

        holder.btnBlock.setOnClickListener { onBlockClick(user, position) }
        holder.btnDelete.setOnClickListener { onDeleteClick(user, position) }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<UserAdmin>) {
        userList = newList.toMutableList()
        notifyDataSetChanged()
    }
}