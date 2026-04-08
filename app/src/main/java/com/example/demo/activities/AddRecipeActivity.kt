package com.example.demo.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.demo.R
import com.example.demo.models.Recipe
import com.google.firebase.firestore.FirebaseFirestore

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var edtRecipeName: EditText
    private lateinit var edtDescription: EditText
    private lateinit var imgFood: ImageView
    private lateinit var layoutIngredients: LinearLayout
    private lateinit var layoutSteps: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var ingredientsList = mutableListOf<String>()
    private var stepsList = mutableListOf<String>()
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String = ""
    private var recipeId: String? = null

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        edtRecipeName = findViewById(R.id.edtRecipeName)
        edtDescription = findViewById(R.id.edtDescription)
        imgFood = findViewById(R.id.imgFood)
        layoutIngredients = findViewById(R.id.layoutIngredients)
        layoutSteps = findViewById(R.id.layoutSteps)
        progressBar = findViewById(R.id.progressBar)

        val btnPublish = findViewById<Button>(R.id.btnPublish)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnAddIngredient = findViewById<TextView>(R.id.btnAddIngredient)
        val btnAddStep = findViewById<TextView>(R.id.btnAddStep)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)

        recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId != null) {
            loadRecipeData(recipeId!!)
            btnPublish.text = "CẬP NHẬT CÔNG THỨC"
        }

        btnClose.setOnClickListener { finish() }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        btnAddIngredient.setOnClickListener {
            showInputDialog("Thêm nguyên liệu", "Ví dụ: Thịt bò") { value ->
                ingredientsList.add(value.lowercase().trim())
                addTextViewToLayout(layoutIngredients, "• $value")
            }
        }

        btnAddStep.setOnClickListener {
            showInputDialog("Thêm bước làm", "Ví dụ: Sơ chế thịt bò") { value ->
                stepsList.add(value)
                addTextViewToLayout(layoutSteps, "${stepsList.size}. $value")
            }
        }

        btnPublish.setOnClickListener {
            val name = edtRecipeName.text.toString().trim()
            if (name.isEmpty() || ingredientsList.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và nguyên liệu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadImageToCloudinary()
            } else if (uploadedImageUrl.isNotEmpty()) {
                saveToFirebase(uploadedImageUrl)
            } else {
                Toast.makeText(this, "Vui lòng chọn ảnh món ăn!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRecipeData(id: String) {
        firestore.collection("recipes").document(id).get()
            .addOnSuccessListener { doc ->
                val recipe = doc.toObject(Recipe::class.java)
                recipe?.let {
                    edtRecipeName.setText(it.name)
                    edtDescription.setText(it.description)
                    uploadedImageUrl = it.image
                    Glide.with(this).load(it.image).into(imgFood)
                    
                    it.ingredients.forEach { ing ->
                        ingredientsList.add(ing)
                        addTextViewToLayout(layoutIngredients, "• $ing")
                    }
                    it.steps.forEach { step ->
                        stepsList.add(step)
                        addTextViewToLayout(layoutSteps, "${stepsList.size}. $step")
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imgFood.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageToCloudinary() {
        progressBar.visibility = View.VISIBLE
        
        MediaManager.get().upload(selectedImageUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    progressBar.visibility = View.GONE
                    uploadedImageUrl = resultData?.get("secure_url").toString()
                    saveToFirebase(uploadedImageUrl)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AddRecipeActivity, "Lỗi upload!", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun saveToFirebase(imageUrl: String) {
        val name = edtRecipeName.text.toString().trim()
        val desc = edtDescription.text.toString().trim()

        val recipeData = hashMapOf(
            "name" to name,
            "description" to desc,
            "image" to imageUrl,
            "ingredients" to ingredientsList,
            "steps" to stepsList,
            "author" to "Admin",
            "createdAt" to System.currentTimeMillis()
        )

        val docRef = if (recipeId == null) {
            firestore.collection("recipes").document()
        } else {
            firestore.collection("recipes").document(recipeId!!)
        }

        docRef.set(recipeData)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Đã lưu!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi Firebase!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addTextViewToLayout(layout: LinearLayout, content: String) {
        val tv = TextView(this)
        tv.text = content
        tv.textSize = 16f
        tv.setPadding(10, 10, 10, 10)
        tv.setTextColor(android.graphics.Color.BLACK)
        layout.addView(tv)
    }

    private fun showInputDialog(title: String, hint: String, onResult: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        val input = EditText(this)
        input.hint = hint
        builder.setView(input)
        builder.setPositiveButton("Thêm") { _, _ ->
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) onResult(text)
        }
        builder.setNegativeButton("Hủy", null)
        builder.show()
    }
}