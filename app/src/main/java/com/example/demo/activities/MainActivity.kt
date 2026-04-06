package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.example.demo.R
import com.example.demo.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        // 1. Setup Categories
        setupCategories()

        // 2. Nút Home (Giữa) -> FeedActivity
        findViewById<LinearLayout>(R.id.btnHome)?.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }

        // 3. Bottom Tabs
        findViewById<LinearLayout>(R.id.tabProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.tabSaved)?.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        // 4. Load món ăn mới nhất từ Firebase
        loadNewRecipes()

        // 5. Nút Notification
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
            Glide.with(this).load(recipe.image).placeholder(R.drawable.choco).into(it)
        }

        // Thêm sự kiện click để vào trang chi tiết
        view.setOnClickListener {
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("recipe_name", recipe.name)
            intent.putExtra("recipe_image", recipe.image)
            intent.putExtra("recipe_author", recipe.author)
            intent.putExtra("recipe_description", recipe.description)
            intent.putStringArrayListExtra("recipe_ingredients", ArrayList(recipe.ingredients))
            intent.putStringArrayListExtra("recipe_steps", ArrayList(recipe.steps))
            startActivity(intent)
        }

        parent.addView(view)
    }

    private fun setupCategories() {
        val cats = mapOf(
            R.id.catDryFood to Pair("Đồ Khô", "🍚"),
            R.id.catSoup to Pair("Đồ Nước", "🍜"),
            R.id.catSnack to Pair("Ăn Vặt", "🧆"),
            R.id.catDessert to Pair("Tráng Miệng", "🍮"),
            R.id.catGrill to Pair("Nướng", "🔥"),
            R.id.catVegan to Pair("Chay", "🥗"),
            R.id.catDrink to Pair("Đồ Uống", "🧋")
        )

        cats.forEach { (id, data) ->
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
}