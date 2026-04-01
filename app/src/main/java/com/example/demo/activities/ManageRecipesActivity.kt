package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R
import com.example.demo.models.Dish
import com.example.demo.adapters.AdminRecipeAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageRecipesActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: AdminRecipeAdapter
    private var recipeList = mutableListOf<Dish>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_recipes)

        db = DatabaseHelper(this)
        val rv = findViewById<RecyclerView>(R.id.rvManageRecipes)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddRecipe)

        rv.layoutManager = LinearLayoutManager(this)

        adapter = AdminRecipeAdapter(recipeList,
            onDeleteClick = { dish ->
                // XỬ LÝ XÓA: Chuyển ID về Int để khớp với hàm delete của DatabaseHelper
                val result = db.deleteRecipeById(dish.id.toInt())
                if (result > 0) {
                    Toast.makeText(this, "Đã xóa ${dish.name}", Toast.LENGTH_SHORT).show()
                    loadData() // Cập nhật lại danh sách ngay lập tức
                } else {
                    Toast.makeText(this, "Lỗi khi xóa món ăn!", Toast.LENGTH_SHORT).show()
                }
            },
            onEditClick = { dish ->
                val intent = Intent(this, AddRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", dish.id)
                intent.putExtra("MODE", "EDIT")
                startActivity(intent)
            }
        )
        rv.adapter = adapter

        // 2. Xử lý nút THÊM MỚI
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }
    }

    // Refresh lại dữ liệu mỗi khi từ màn hình Thêm/Sửa quay về
    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        recipeList.clear()
        val cursor = db.getAllRecipes()

        if (cursor != null && cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex("id")
            val nameIndex = cursor.getColumnIndex("name")
            val infoIndex = cursor.getColumnIndex("ingredients") // Cột ingredients trong DB map vào 'info' của Dish

            do {
                if (idIndex >= 0 && nameIndex >= 0) {
                    val id = cursor.getInt(idIndex)
                    val name = cursor.getString(nameIndex) ?: ""
                    val info = if (infoIndex >= 0) cursor.getString(infoIndex) ?: "" else ""

                    // Khớp với Dish(id, name, imgRes, info)
                    // Vì SQLite không lưu được R.drawable trực tiếp dễ dàng, để mặc định là ảnh choco
                    recipeList.add(Dish(id = id, name = name, imgRes = R.drawable.choco, info = info))
                }
            } while (cursor.moveToNext())
        }
        cursor?.close()
        adapter.notifyDataSetChanged()
    }
}