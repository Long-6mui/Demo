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
import com.example.demo.models.Dish

class SavedActivity : AppCompatActivity() {

    private lateinit var adapter: SavedAdapter
    private lateinit var rvSavedRecipes: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etSearch: EditText
    private var fullList = mutableListOf<Dish>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved)
        dbHelper = DatabaseHelper(this)
        rvSavedRecipes = findViewById(R.id.rvSavedRecipes)
        etSearch = findViewById(R.id.etSearchSaved)
        rvSavedRecipes.layoutManager = LinearLayoutManager(this)
        checkInitialData()
        loadData()
        setupSearch()
    }

    private fun checkInitialData() {
        val cursorCheck = dbHelper.getAllSavedRecipes()
        if (cursorCheck.count == 0) {
            dbHelper.addSavedRecipe("Bún bò Huế", "60 phút • Khó")
            dbHelper.addSavedRecipe("Gỏi cuốn", "20 phút • Dễ làm")
            dbHelper.addSavedRecipe("Bánh xèo", "45 phút • Trung bình")
            dbHelper.addSavedRecipe("Phở Bò", "60 phút • Trung bình")
            dbHelper.addSavedRecipe("Pasta Carbonara", "30 phút • Dễ làm")
        }
        cursorCheck.close()
    }

    private fun loadData() {
        fullList.clear()
        val cursor = dbHelper.getAllSavedRecipes()


        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SAVED_NAME))
                val info = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SAVED_INFO))

                val imgRes = when {
                    name.contains("Bún bò", ignoreCase = true) -> R.drawable.bunbo
                    name.contains("Bánh xèo", ignoreCase = true) -> R.drawable.banhxeo
                    name.contains("Gỏi cuốn", ignoreCase = true) -> R.drawable.goicuon
                    name.contains("Phở", ignoreCase = true) -> R.drawable.pho
                    else -> R.drawable.miy
                }

                fullList.add(Dish(0, name, imgRes, info))
            } while (cursor.moveToNext())
        }
        cursor.close()

        adapter = SavedAdapter(fullList)
        rvSavedRecipes.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val filteredList = fullList.filter { it.name.contains(query, ignoreCase = true) }
                adapter.updateList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}