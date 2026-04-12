package com.example.demo.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.adapters.RecipeAdapter
import com.example.demo.models.Recipe
import com.google.firebase.firestore.Query
import java.util.Locale

class AllRecipesActivity : BaseActivity() {

    private lateinit var rvAllRecipes: RecyclerView
    private lateinit var edtSearchAll: EditText
    private lateinit var btnBack: ImageButton
    private var recipeList = mutableListOf<Recipe>()
    private var filteredList = mutableListOf<Recipe>()
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_recipes)

        initViews()
        setupRecyclerView()
        loadAllRecipes()
        setupSearch()

        btnBack.setOnClickListener { finish() }
    }

    private fun initViews() {
        rvAllRecipes = findViewById(R.id.rvAllRecipes)
        edtSearchAll = findViewById(R.id.edtSearchAll)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter(filteredList)
        // Hiển thị dạng lưới 2 cột cho đẹp mắt
        rvAllRecipes.layoutManager = GridLayoutManager(this, 2)
        rvAllRecipes.adapter = adapter
    }

    private fun loadAllRecipes() {
        db.collection("recipes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                recipeList.clear()
                for (document in snapshots) {
                    val recipe = document.toObject(Recipe::class.java).copy(id = document.id)
                    recipeList.add(recipe)
                }
                filterList(edtSearchAll.text.toString())
            }
    }

    private fun setupSearch() {
        edtSearchAll.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterList(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(recipeList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            for (recipe in recipeList) {
                if (recipe.name.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    recipe.author.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredList.add(recipe)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}