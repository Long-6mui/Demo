package com.example.demo.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.R
import com.example.demo.models.Recipe
import com.example.demo.activities.RecipeDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedAdapter(
    private val recipeList: MutableList<Recipe>,
    private val isSavedScreen: Boolean = false
) : RecyclerView.Adapter<SavedAdapter.RecipeViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgRecipe: ImageView = view.findViewById(R.id.imgRecipe)
        val tvName: TextView = view.findViewById(R.id.tvRecipeName)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
        val btnAction: ImageButton = view.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        val context = holder.itemView.context
        val userId = auth.currentUser?.uid ?: return

        holder.tvName.text = recipe.name
        holder.tvInfo.text = recipe.description
        
        Glide.with(context)
            .load(recipe.image)
            .placeholder(R.drawable.choco)
            .into(holder.imgRecipe)

        // Click xem chi tiết
        holder.itemView.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipe_name", recipe.name)
            intent.putExtra("recipe_image", recipe.image)
            intent.putExtra("recipe_author", recipe.author)
            intent.putExtra("recipe_description", recipe.description)
            intent.putStringArrayListExtra("recipe_ingredients", ArrayList(recipe.ingredients))
            intent.putStringArrayListExtra("recipe_steps", ArrayList(recipe.steps))
            context.startActivity(intent)
        }

        if (isSavedScreen) {
            holder.btnAction.setImageResource(R.drawable.ic_delete)
            holder.btnAction.setOnClickListener {
                // Xóa khỏi danh sách yêu thích trên Firebase
                db.collection("Users").document(userId)
                    .collection("SavedRecipes").document(recipe.id)
                    .delete()
                    .addOnSuccessListener {
                        recipeList.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                        Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            // Màn hình Gợi ý: Kiểm tra xem món này đã lưu chưa
            db.collection("Users").document(userId)
                .collection("SavedRecipes").document(recipe.id)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        holder.btnAction.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        holder.btnAction.setImageResource(R.drawable.ic_heart_outline)
                    }
                }

            holder.btnAction.setOnClickListener {
                val docRef = db.collection("Users").document(userId)
                    .collection("SavedRecipes").document(recipe.id)

                docRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        docRef.delete()
                        holder.btnAction.setImageResource(R.drawable.ic_heart_outline)
                        Toast.makeText(context, "Đã bỏ lưu!", Toast.LENGTH_SHORT).show()
                    } else {
                        docRef.set(recipe)
                        holder.btnAction.setImageResource(R.drawable.ic_heart_filled)
                        Toast.makeText(context, "Đã lưu!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = recipeList.size
}