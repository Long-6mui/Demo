package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
        val context = holder.itemView.context
        val isSelected = selectedIds.contains(ingredient.ingreID)

        holder.txtName.text = ingredient.nameIngre

        if (isSelected) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            holder.txtName.setTextColor(ContextCompat.getColor(context, R.color.colorOnPrimary))
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSurface))
            holder.txtName.setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
        }

        holder.card.setOnClickListener {
            if (isSelected) {
                selectedIds.remove(ingredient.ingreID)
            } else {
                selectedIds.add(ingredient.ingreID)
            }
            notifyItemChanged(position)
            onChanged()
        }
    }

    override fun getItemCount() = ingredients.size
}