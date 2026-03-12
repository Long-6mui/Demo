package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.CategoryIngre
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class IngredientsAdapter(
    private val categories: List<CategoryIngre>,
    private val selectedIds: MutableSet<String>,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<IngredientsAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHeading: TextView = view.findViewById(R.id.txtCategoryHeading)
        val txtArrow: TextView = view.findViewById(R.id.txtArrow)
        val rvChips: RecyclerView = view.findViewById(R.id.rvChips)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.txtHeading.text = category.cateName
        holder.txtArrow.text = if (category.isExpanded) "▲" else "▼"
        holder.rvChips.visibility = if (category.isExpanded) View.VISIBLE else View.GONE

        val flexManager = FlexboxLayoutManager(holder.itemView.context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }
        holder.rvChips.layoutManager = flexManager
        holder.rvChips.adapter = IngredientChipAdapter(
            category.ingredients, selectedIds
        ) { onSelectionChanged(selectedIds.size) }

        holder.itemView.setOnClickListener {
            category.isExpanded = !category.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = categories.size
}