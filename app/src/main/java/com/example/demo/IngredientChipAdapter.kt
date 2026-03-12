package com.example.demo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Ingredient

class IngredientChipAdapter(
    private val ingredients: List<Ingredient>,
    private val selectedIds: MutableSet<String>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<IngredientChipAdapter.ChipViewHolder>() {

    inner class ChipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.chipCard)
        val txtName: TextView = view.findViewById(R.id.txtIngredientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_chip, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val ingredient = ingredients[position]
        val isSelected = selectedIds.contains(ingredient.ingreID)

        holder.txtName.text = ingredient.nameIngre

        if (isSelected) {
            holder.card.setCardBackgroundColor(Color.parseColor("#1A3A2A"))
            holder.txtName.setTextColor(Color.WHITE)
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.txtName.setTextColor(Color.parseColor("#1A3A2A"))
        }

        holder.card.setOnClickListener {
            if (isSelected) selectedIds.remove(ingredient.ingreID)
            else selectedIds.add(ingredient.ingreID)
            notifyItemChanged(position)
            onChanged()
        }
    }

    override fun getItemCount() = ingredients.size
}