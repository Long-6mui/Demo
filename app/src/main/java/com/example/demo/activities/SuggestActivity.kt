package com.example.demo.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Recipe
import com.example.demo.adapters.SavedAdapter
import com.google.firebase.firestore.FirebaseFirestore

class SuggestActivity : BaseActivity() {

    private lateinit var rvSuggestions: RecyclerView
    private val suggestedList = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggest)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        rvSuggestions = findViewById(R.id.rvSuggestions)

        // Ánh xạ nút Back
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack?.setOnClickListener { finish() }

        rvSuggestions.layoutManager = LinearLayoutManager(this)

        // Vừa vào là tự load món ngay từ Firebase
        loadSuggestionsFromFirebase()
    }

    private fun loadSuggestionsFromFirebase() {
        val selectedIngredients = intent.getStringArrayListExtra("selectedIds") ?: arrayListOf()

        if (selectedIngredients.isEmpty()) {
            Toast.makeText(this, "Không có nguyên liệu nào được chọn!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("recipes")
            .get()
            .addOnSuccessListener { snapshots ->
                suggestedList.clear()
                for (doc in snapshots) {
                    val recipe = doc.toObject(Recipe::class.java).copy(id = doc.id)
                    
                    // Thuật toán lọc: Nếu món ăn chứa bất kỳ nguyên liệu nào trong danh sách được chọn
                    val hasMatch = recipe.ingredients.any { ingredient ->
                        selectedIngredients.any { selected ->
                            ingredient.contains(selected, ignoreCase = true)
                        }
                    }
                    
                    if (hasMatch) {
                        suggestedList.add(recipe)
                    }
                }

                if (suggestedList.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy món ăn phù hợp trên hệ thống!", Toast.LENGTH_LONG).show()
                }

                // Truyền List<Recipe> vào SavedAdapter (đã khớp kiểu dữ liệu)
                val adapter = SavedAdapter(suggestedList, false)
                rvSuggestions.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi khi tải dữ liệu từ Firebase", Toast.LENGTH_SHORT).show()
            }
    }
}