package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.Recipe

class ManageRecipeAdapter(
    private var list: MutableList<Recipe>,
    private val onEditClick: (Recipe) -> Unit,
    private val onDeleteClick: (Recipe, Int) -> Unit
) : RecyclerView.Adapter<ManageRecipeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgRecipe: ImageView = view.findViewById(R.id.imgRecipe)
        val txtRecipeName: TextView = view.findViewById(R.id.txtRecipeName)
        // Đổi từ ImageButton sang TextView để khớp với XML
        val btnEdit: TextView = view.findViewById(R.id.btnEdit)
        val btnDelete: TextView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = list[position]
        holder.imgRecipe.setImageResource(recipe.image)
        holder.txtRecipeName.text = recipe.name

        // Xử lý sự kiện Sửa
        holder.btnEdit.setOnClickListener { onEditClick(recipe) }

        // Xử lý sự kiện Xóa
        holder.btnDelete.setOnClickListener { onDeleteClick(recipe, position) }
    }

    override fun getItemCount(): Int = list.size

    fun removeItem(position: Int) {
        if (position in 0 until list.size) {
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, list.size)
        }
    }
}