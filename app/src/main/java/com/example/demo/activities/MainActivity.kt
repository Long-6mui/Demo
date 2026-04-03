package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.demo.R
import com.example.demo.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupCategories()

        // 1. Sửa lỗi ID txtSeeAll
        findViewById<TextView>(R.id.txtSeeAll)?.apply {
            text = "Xem tất cả →"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SavedActivity::class.java))
            }
        }

        // 2. Sửa lỗi ép kiểu txtFavoriteTitle (Nó là LinearLayout, lấy TextView con bên trong)
        val layoutTitle = findViewById<LinearLayout>(R.id.txtFavoriteTitle)
        if (layoutTitle != null) {
            for (i in 0 until layoutTitle.childCount) {
                val child = layoutTitle.getChildAt(i)
                if (child is TextView && child.id != R.id.txtSeeAll) {
                    child.text = "Món ăn mới nhất"
                    break
                }
            }
        }

        // 3. Load món ăn mới
        loadNewRecipes()

        setupBottomNav()

        findViewById<ImageButton>(R.id.btnNotification)?.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    private fun loadNewRecipes() {
        val listFavorites = findViewById<LinearLayout>(R.id.listFavorites) ?: return
        
        db.collection("recipes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                listFavorites.removeAllViews() 
                
                for (document in snapshots) {
                    val recipe = document.toObject(Recipe::class.java).copy(id = document.id)
                    addRecipeCard(listFavorites, recipe)
                }
            }
    }

    private fun addRecipeCard(parent: LinearLayout, recipe: Recipe) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_favorite_horizontal, parent, false)
        
        val img = view.findViewById<ImageView>(R.id.imgRecipe)
        val name = view.findViewById<TextView>(R.id.txtRecipeName)
        
        name?.text = recipe.name
        img?.let {
            Glide.with(this)
                .load(recipe.image)
                .placeholder(R.drawable.choco)
                .into(it)
        }

        parent.addView(view)
    }

    private fun setupCategories() {
        val categories = mapOf(
            R.id.catDryFood  to Pair("Đồ Khô",      "🍚"),
            R.id.catSoup     to Pair("Đồ Nước",     "🍜"),
            R.id.catSnack    to Pair("Ăn Vặt",      "🧆"),
            R.id.catDessert  to Pair("Tráng Miệng", "🍮"),
            R.id.catGrill    to Pair("Nướng",        "🔥"),
            R.id.catVegan    to Pair("Chay",         "🥗"),
            R.id.catDrink    to Pair("Đồ Uống",     "🧋"),
        )

        categories.forEach { (id, data) ->
            findViewById<LinearLayout>(id)?.setOnClickListener {
                val intent = Intent(this, CategoryActivity::class.java)
                intent.putExtra("category", data.first)
                intent.putExtra("emoji", data.second)
                startActivity(intent)
            }
        }
        
        findViewById<LinearLayout>(R.id.catMore)?.setOnClickListener {
            startActivity(Intent(this, IngredientsActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.tabProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.tabSaved)?.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnHome)?.setOnClickListener {
            // Đã ở Home rồi
        }
    }
}