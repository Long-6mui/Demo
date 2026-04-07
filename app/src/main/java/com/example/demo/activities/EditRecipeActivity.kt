package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.Recipe
import com.example.demo.adapters.ManageRecipeAdapter
import com.google.firebase.firestore.FirebaseFirestore

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var adapter: ManageRecipeAdapter
    private val recipeList = mutableListOf<Recipe>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val toolbar = findViewById<Toolbar>(R.id.toolbarManage)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerManageRecipes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ManageRecipeAdapter(
            recipeList,
            onEditClick = { recipe ->
                val intent = Intent(this, AddRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe.id)
                startActivity(intent)
            },
            onDeleteClick = { recipe, position ->
                showDeleteDialog(recipe, position)
            }
        )
        
        recyclerView.adapter = adapter
        loadRecipesFromFirebase()
    }

    private fun loadRecipesFromFirebase() {
        db.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                recipeList.clear()
                for (document in result) {
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val description = document.getString("description") ?: ""
                    val author = document.getString("author") ?: "Admin"
                    val ingredients = document.get("ingredients") as? List<String> ?: emptyList()
                    val steps = document.get("steps") as? List<String> ?: emptyList()
                    val createdAt = document.getLong("createdAt") ?: 0L

                    val recipe = Recipe(document.id, name, image, author, description, ingredients, steps, createdAt)
                    recipeList.add(recipe)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showDeleteDialog(recipe: Recipe, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Xóa công thức")
            .setMessage("Bạn có chắc chắn muốn xóa món '${recipe.name}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                // Xóa trên Firebase
                db.collection("recipes").document(recipe.id)
                    .delete()
                    .addOnSuccessListener {
                        adapter.removeItem(position)
                        Toast.makeText(this, "✅ Đã xóa hoàn toàn!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadRecipesFromFirebase()
    }
}