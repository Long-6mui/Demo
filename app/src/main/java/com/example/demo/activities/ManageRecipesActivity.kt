package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.Recipe
import com.example.demo.adapters.ManageRecipeAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ManageRecipesActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: ManageRecipeAdapter
    private var recipeList = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_recipes)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val rv = findViewById<RecyclerView>(R.id.rvManageRecipes)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddRecipe)

        rv.layoutManager = LinearLayoutManager(this)

        // Sử dụng ManageRecipeAdapter (đã hỗ trợ Glide và dữ liệu Firebase)
        adapter = ManageRecipeAdapter(recipeList,
            onEditClick = { recipe ->
                val intent = Intent(this, AddRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe.id)
                startActivity(intent)
            },
            onDeleteClick = { recipe, position ->
                showDeleteDialog(recipe, position)
            }
        )
        rv.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {
        // Lấy dữ liệu từ Firebase để đồng nhất với User
        firestore.collection("recipes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                recipeList.clear()
                snapshots?.forEach { doc ->
                    val recipe = doc.toObject(Recipe::class.java).copy(id = doc.id)
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
                firestore.collection("recipes").document(recipe.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã xóa thành công!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Lỗi khi xóa!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}