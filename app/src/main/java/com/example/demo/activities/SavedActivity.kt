package com.example.demo.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R
import com.example.demo.adapters.SavedAdapter
import com.example.demo.models.Dish


class SavedActivity : AppCompatActivity() {

    private lateinit var rvSavedRecipes: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etSearch: EditText
    private var fullList = mutableListOf<Dish>()
    private lateinit var adapter: SavedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        dbHelper = DatabaseHelper(this)
        rvSavedRecipes = findViewById(R.id.rvSavedRecipes)
        etSearch = findViewById(R.id.etSearchSaved)

        rvSavedRecipes.layoutManager = LinearLayoutManager(this)

        loadData()
        setupSearch()
    }

    private fun loadData() {
        fullList.clear()
        val cursor = dbHelper.getAllSavedRecipes()

        if (cursor.moveToFirst()) {
            // Lấy index cẩn thận để tránh lỗi crash
            val idIndex = cursor.getColumnIndex(DatabaseHelper.SAVED_ID)
            val nameIndex = cursor.getColumnIndex(DatabaseHelper.SAVED_NAME)
            val infoIndex = cursor.getColumnIndex(DatabaseHelper.SAVED_INFO)

            if (idIndex != -1 && nameIndex != -1 && infoIndex != -1) {
                do {
                    val id = cursor.getInt(idIndex)
                    val name = cursor.getString(nameIndex) ?: ""
                    val info = cursor.getString(infoIndex) ?: ""

                    val imgRes = when {
                        name.contains("Bún bò", ignoreCase = true) -> R.drawable.bunbo
                        name.contains("Bánh xèo", ignoreCase = true) -> R.drawable.banhxeo
                        name.contains("Phở", ignoreCase = true) -> R.drawable.pho
                        else -> R.drawable.choco
                    }
                    fullList.add(Dish(id, name, imgRes, info))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()

        // QUAN TRỌNG: Truyền true vào tham số thứ 2 để hiện nút XÓA
        adapter = SavedAdapter(fullList.toMutableList(), true)
        rvSavedRecipes.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filteredList = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter { it.name.contains(query, ignoreCase = true) }
                }

                // Cập nhật lại adapter với list đã lọc và vẫn giữ chế độ màn hình Saved (true)
                rvSavedRecipes.adapter = SavedAdapter(filteredList.toMutableList(), true)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}