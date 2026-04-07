package com.example.demo.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedActivity : BaseActivity() {

    private lateinit var rvSavedRecipes: RecyclerView
    private lateinit var etSearch: EditText
    private var fullList = mutableListOf<Recipe>()
    private lateinit var adapter: SavedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        rvSavedRecipes = findViewById(R.id.rvSavedRecipes)
        etSearch = findViewById(R.id.etSearchSaved)
        val btnBack = findViewById<ImageButton>(R.id.btnBackSaved)

        btnBack?.setOnClickListener {
            // Kết thúc Activity này để quay lại màn hình trước đó
            onBackPressedDispatcher.onBackPressed()
        }
        
        rvSavedRecipes.layoutManager = LinearLayoutManager(this)

        loadSavedFromFirebase()
        setupSearch()
    }

    // Trong SavedActivity.kt
    private fun loadSavedFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        // Thay .addSnapshotListener bằng .get() để tránh tự động load lại khi đang xóa
        db.collection("Users").document(userId)
            .collection("SavedRecipes")
            .get()
            .addOnSuccessListener { snapshots ->
                fullList.clear()
                snapshots?.forEach { doc ->
                    val recipe = doc.toObject(Recipe::class.java).copy(id = doc.id)
                    fullList.add(recipe)
                }

                // Khởi tạo adapter một lần
                adapter = SavedAdapter(fullList, true)
                rvSavedRecipes.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
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
                rvSavedRecipes.adapter = SavedAdapter(filteredList.toMutableList(), true)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}