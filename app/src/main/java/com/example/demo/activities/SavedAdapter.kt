package com.example.demo.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Dish

class SavedAdapter(private var dishList: List<Dish>) :
    RecyclerView.Adapter<SavedAdapter.DishViewHolder>() {

    fun updateList(newList: List<Dish>) {
        dishList = newList
        notifyDataSetChanged()
    }

    class DishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDish: ImageView = view.findViewById(R.id.imgRecipe) // Đổi ở đây
        val tvName: TextView = view.findViewById(R.id.tvRecipeName) // Đổi ở đây
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)    // Đổi ở đây
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        // Đảm bảo tên file layout này là item_saved_recipe.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_recipe, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishList[position]
        holder.tvName.text = dish.name
        holder.tvInfo.text = dish.info

        // Sử dụng imgRes cho khớp với model Dish.kt của bạn
        holder.imgDish.setImageResource(dish.imgRes)
    }



    override fun getItemCount(): Int = dishList.size
}