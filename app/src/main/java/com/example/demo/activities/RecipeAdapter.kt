package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.Recipe

class RecipeAdapter(private val list: List<Recipe>) :
    RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imgFood: ImageView = view.findViewById(R.id.imgFood)
        val txtFoodName: TextView = view.findViewById(R.id.txtFoodName)
        val txtAuthor: TextView = view.findViewById(R.id.txtAuthor)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_post, parent, false)

        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val recipe = list[position]

        holder.imgFood.setImageResource(recipe.image)
        holder.txtFoodName.text = recipe.name
        holder.txtAuthor.text = recipe.author

    }

    override fun getItemCount(): Int {
        return list.size
    }
}