package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Dish
import com.bumptech.glide.Glide

class AdminRecipeAdapter(
    private val list: List<Dish>,
    private val onDeleteClick: (Dish) -> Unit,
    private val onEditClick: (Dish) -> Unit
) : RecyclerView.Adapter<AdminRecipeAdapter.AdminViewHolder>() {

    class AdminViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val img: ImageView = v.findViewById(R.id.imgAdminRecipe)
        val name: TextView = v.findViewById(R.id.tvAdminRecipeName)
        val btnEdit: ImageButton = v.findViewById(R.id.btnEditRecipe)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDeleteRecipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_recipe, parent, false)
        return AdminViewHolder(v)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val dish = list[position]

        // Gán tên món ăn
        holder.name.text = dish.name

        // Load ảnh: Nếu link ảnh trống thì hiện ảnh mặc định
        holder.img.setImageResource(dish.imgRes)

        // Sự kiện khi bấm nút Xóa
        holder.btnDelete.setOnClickListener {
            onDeleteClick(dish)
        }

        // Sự kiện khi bấm nút Sửa
        holder.btnEdit.setOnClickListener {
            onEditClick(dish)
        }
    }

    override fun getItemCount() = list.size
}