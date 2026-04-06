package com.example.demo.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.example.demo.R

class RecipeDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        val imgRecipe = findViewById<ImageView>(R.id.imgRecipe)
        val txtRecipeName = findViewById<TextView>(R.id.txtRecipeName)
        val txtAuthor = findViewById<TextView>(R.id.txtAuthor)
        val txtDescription = findViewById<TextView>(R.id.txtDescription)
        val txtIngredients = findViewById<TextView>(R.id.txtIngredients)
        val txtSteps = findViewById<TextView>(R.id.txtSteps)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        // Thiết lập nút quay lại
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Nhận dữ liệu từ Intent
        val recipeName = intent.getStringExtra("recipe_name")
        val recipeImage = intent.getStringExtra("recipe_image")
        val recipeAuthor = intent.getStringExtra("recipe_author")
        val recipeDescription = intent.getStringExtra("recipe_description")
        val recipeIngredients = intent.getStringArrayListExtra("recipe_ingredients")
        val recipeSteps = intent.getStringArrayListExtra("recipe_steps")

        // Hiển thị dữ liệu
        txtRecipeName.text = recipeName
        txtAuthor.text = "Bởi: $recipeAuthor"
        txtDescription.text = recipeDescription

        Glide.with(this)
            .load(recipeImage)
            .placeholder(R.drawable.choco)
            .into(imgRecipe)

        // Định dạng danh sách nguyên liệu
        val ingredientsText = recipeIngredients?.joinToString("\n") { "• $it" }
        txtIngredients.text = ingredientsText ?: "Không có thông tin nguyên liệu"

        // Định dạng danh sách các bước
        val stepsText = recipeSteps?.mapIndexed { index, step -> "${index + 1}. $step" }?.joinToString("\n\n")
        txtSteps.text = stepsText ?: "Không có thông tin cách làm"
    }
}