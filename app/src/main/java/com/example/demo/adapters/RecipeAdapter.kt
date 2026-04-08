package com.example.demo.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.R
import com.example.demo.models.Recipe
import com.example.demo.activities.RecipeDetailActivity

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

        // Sử dụng Glide để load ảnh từ URL (Cloudinary/Firebase)
        Glide.with(holder.itemView.context)
            .load(recipe.image)
            .placeholder(R.drawable.choco) 
            .error(R.drawable.choco)       
            .into(holder.imgFood)

        holder.txtFoodName.text = recipe.name
        holder.txtAuthor.text = recipe.author

        // Bắt sự kiện click để mở trang chi tiết
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RecipeDetailActivity::class.java)
            intent.putExtra("recipe_name", recipe.name)
            intent.putExtra("recipe_image", recipe.image)
            intent.putExtra("recipe_author", recipe.author)
            intent.putExtra("recipe_description", recipe.description)
            intent.putStringArrayListExtra("recipe_ingredients", ArrayList(recipe.ingredients))
            intent.putStringArrayListExtra("recipe_steps", ArrayList(recipe.steps))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}