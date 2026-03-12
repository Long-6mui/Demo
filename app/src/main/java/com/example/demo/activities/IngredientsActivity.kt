package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.adapters.IngredientsAdapter
import com.example.demo.models.CategoryIngre
import com.example.demo.models.Ingredient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IngredientsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val selectedIds = mutableSetOf<String>()
    private lateinit var txtCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        txtCount = findViewById(R.id.txtSelectedCount)
        val rvIngredients = findViewById<RecyclerView>(R.id.rvIngredients)
        val btnSuggest = findViewById<Button>(R.id.btnSuggest)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        rvIngredients.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        btnSuggest.setOnClickListener {
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "Chọn ít nhất 1 nguyên liệu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveAndSuggest()
        }

        loadCategoriesAndIngredients(rvIngredients)
    }

    private fun loadCategoriesAndIngredients(rv: RecyclerView) {
        //  1: load CategoryIngre
        db.collection("category_ingre")
            .get()
            .addOnSuccessListener { catResult ->
                val categoryList = catResult.documents.map { doc ->
                    CategoryIngre(
                        cateinID = doc.id,
                        cateName = doc.getString("cateName") ?: ""
                    )
                }.toMutableList()

                // 2: load Ingredient rồi gắn vào đúng category
                db.collection("ingredients")
                    .get()
                    .addOnSuccessListener { ingreResult ->
                        val allIngredients = ingreResult.documents.map { doc ->
                            Ingredient(
                                ingreID = doc.id,
                                nameIngre = doc.getString("nameIngre") ?: "",
                                cateinID = doc.getString("cateinID") ?: ""

                            )
                        }

                        // Gắn nguyên liệu vào đúng category
                        // Thay đoạn filledCategories
                        val filledCategories = categoryList.map { cat ->
                            cat.apply {
                                ingredients.addAll(
                                    allIngredients.filter { it.cateinID == cat.cateinID }
                                )
                            }
                        }.filter { it.ingredients.isNotEmpty() }

                        rv.adapter = IngredientsAdapter(filledCategories, selectedIds) { count ->
                            txtCount.text = if (count == 0) "Chưa chọn nguyên liệu nào"
                            else "Đã chọn $count nguyên liệu"
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Lỗi tải nguyên liệu", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAndSuggest() {
        val userId = auth.currentUser?.uid

        // Lưu user_ingredients lên Firestore nếu đã login
        if (userId != null) {
            db.collection("user_ingredients").document(userId)
                .set(mapOf("ingredients" to selectedIds.toList()))
                .addOnSuccessListener {
                    goToSuggest()
                }
                .addOnFailureListener {
                    // Vẫn chuyển màn dù lưu lỗi
                    goToSuggest()
                }
        } else {
            goToSuggest()
        }
    }

    private fun goToSuggest() {
        val intent = Intent(this, SuggestActivity::class.java)
        intent.putStringArrayListExtra("selectedIds", ArrayList(selectedIds))
        startActivity(intent)
    }
}