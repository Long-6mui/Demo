package com.example.demo.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R
import com.example.demo.adapters.SavedAdapter
import com.example.demo.models.Dish

class SuggestActivity : AppCompatActivity() {

    private lateinit var rvSuggestions: RecyclerView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggest)

        dbHelper = DatabaseHelper(this)
        rvSuggestions = findViewById(R.id.rvSuggestions)

        // Ánh xạ nút Back
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        rvSuggestions.layoutManager = LinearLayoutManager(this)

        // Vừa vào là tự load món ngay
        loadSuggestions()
    }

    private fun loadSuggestions() {
        val suggestedList = getSuggestionsFromDb()

        if (suggestedList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy món ăn phù hợp!", Toast.LENGTH_LONG).show()
        }

        // SỬA DÒNG NÀY: Truyền thêm false để báo đây KHÔNG PHẢI màn hình Saved (Yêu thích)
        // Nó sẽ hiện icon Trái Tim thay vì icon Thùng Rác
        val adapter = SavedAdapter(suggestedList.toMutableList(), false)

        rvSuggestions.adapter = adapter
    }

    private fun getSuggestionsFromDb(): List<Dish> {
        val list = mutableListOf<Dish>()
        val selectedIngredients = intent.getStringArrayListExtra("selectedIds")

        if (selectedIngredients.isNullOrEmpty()) return list

        val db = dbHelper.readableDatabase

        val whereClause = selectedIngredients.joinToString(" OR ") { "LOWER(ingredients) LIKE ?" }

        val whereArgs = selectedIngredients.map { "%${it.lowercase().trim()}%" }.toTypedArray()

        android.util.Log.d("DEBUG_RECIPE", "Từ khóa User gửi sang: ${selectedIngredients.joinToString()}")

        val cursor = db.rawQuery("SELECT * FROM recipes WHERE $whereClause", whereArgs)

        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow("id")
            val nameIdx = cursor.getColumnIndexOrThrow("name")
            val infoIdx = cursor.getColumnIndexOrThrow("ingredients")

            do {
                val id = cursor.getInt(idIdx)
                val name = cursor.getString(nameIdx)
                val info = cursor.getString(infoIdx)
                list.add(Dish(id, name, getImgByName(name), info))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
    private fun getImgByName(name: String): Int {
        val n = name.lowercase()
        return when {
            n.contains("bún bò") -> R.drawable.bunbo
            n.contains("phở") -> R.drawable.pho
            n.contains("bánh xèo") -> R.drawable.banhxeo
            n.contains("gỏi cuốn") -> R.drawable.goicuon
            else -> R.drawable.choco
        }
    }
}