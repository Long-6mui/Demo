package com.example.demo.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.Database.DatabaseHelper
import com.example.demo.models.Dish

class SavedAdapter(private val dishList: List<Dish>) :
    RecyclerView.Adapter<SavedAdapter.DishViewHolder>() {

    class DishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDish: ImageView = view.findViewById(R.id.imgRecipe)
        val tvName: TextView = view.findViewById(R.id.tvRecipeName)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_recipe, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishList[position]
        holder.tvName.text = dish.name
        holder.tvInfo.text = dish.info
        holder.imgDish.setImageResource(dish.imgRes)

        holder.btnDelete.setOnClickListener {
            val context = holder.itemView.context
            val db = DatabaseHelper(context)

            db.deleteSavedRecipe(dish.name)

            if (dishList is MutableList) {
                (dishList as MutableList).removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, dishList.size)
            }
        }
    }

    override fun getItemCount(): Int = dishList.size
}