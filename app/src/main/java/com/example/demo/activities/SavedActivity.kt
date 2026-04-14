package com.example.demo.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Recipe
import com.example.demo.adapters.SavedAdapter

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
            onBackPressedDispatcher.onBackPressed()
        }

        rvSavedRecipes.layoutManager = LinearLayoutManager(this)
        adapter = SavedAdapter(fullList, true)
        rvSavedRecipes.adapter = adapter

        loadSavedFromFirebase()
        setupSearch()
    }

    private fun loadSavedFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        // SỬ DỤNG addSnapshotListener ĐỂ LẮNG NGHE THỜI GIAN THỰC
        db.collection("Users").document(userId)
            .collection("SavedRecipes")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val recipesToCheck = snapshots?.documents ?: emptyList()
                if (recipesToCheck.isEmpty()) {
                    fullList.clear()
                    adapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                val validRecipes = mutableListOf<Recipe>()
                var checkedCount = 0

                for (doc in recipesToCheck) {
                    val recipeId = doc.id
                    val recipeData = doc.toObject(Recipe::class.java)?.copy(id = recipeId)

                    if (recipeData != null) {
                        // KIỂM TRA XEM CÔNG THỨC CÒN TỒN TẠI TRÊN HỆ THỐNG KHÔNG
                        db.collection("recipes").document(recipeId).get().addOnSuccessListener { mainDoc ->
                            if (!mainDoc.exists()) {
                                // ADMIN ĐÃ XÓA -> Xóa ngay lập tức khỏi kho lưu của User
                                db.collection("Users").document(userId)
                                    .collection("SavedRecipes").document(recipeId).delete()
                            } else {
                                validRecipes.add(recipeData)
                            }

                            checkedCount++
                            // Khi đã kiểm tra xong tất cả
                            if (checkedCount == recipesToCheck.size) {
                                fullList.clear()
                                fullList.addAll(validRecipes)
                                // Sắp xếp theo tên hoặc thời gian nếu cần
                                fullList.sortBy { it.name }
                                adapter.notifyDataSetChanged()
                            }
                        }
                    } else {
                        checkedCount++
                    }
                }
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
                adapter = SavedAdapter(filteredList.toMutableList(), true)
                rvSavedRecipes.adapter = adapter
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}