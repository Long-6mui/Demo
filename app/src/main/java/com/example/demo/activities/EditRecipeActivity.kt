package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.Recipe
import com.example.demo.adapters.ManageRecipeAdapter
import kotlin.jvm.java

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var adapter: ManageRecipeAdapter
    private val recipeList = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarManage)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerManageRecipes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Dữ liệu mẫu (Sau này bạn sẽ load từ Firebase/Database)
        loadRecipes()

        adapter = ManageRecipeAdapter(
            recipeList,
            onEditClick = { recipe ->
                // Xử lý Sửa: Chuyển sang AddRecipeActivity kèm dữ liệu
                val intent = Intent(this, AddRecipeActivity::class.java)
                intent.putExtra("MODE", "EDIT")
                intent.putExtra("RECIPE_NAME", recipe.name)
                startActivity(intent)
            },
            onDeleteClick = { recipe, position ->
                // Xử lý Xóa: Hiện thông báo xác nhận
                showDeleteDialog(recipe, position)
            }
        )
        
        recyclerView.adapter = adapter
    }

    private fun loadRecipes() {
        // Tạm thời lấy dữ liệu mẫu giống trang Profile
        recipeList.add(Recipe(R.drawable.goicuon, "Gỏi Cuốn Miền Tây", "Admin"))
        recipeList.add(Recipe(R.drawable.garan, "Gà Rán", "Admin"))
        recipeList.add(Recipe(R.drawable.cua, "Cua Rang Me", "Admin"))
    }

    private fun showDeleteDialog(recipe: Recipe, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Xóa công thức")
            .setMessage("Bạn có chắc chắn muốn xóa món '${recipe.name}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                adapter.removeItem(position)
                Toast.makeText(this, " Đã xóa ${recipe.name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
