package com.example.demo.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.isEmpty
import kotlin.text.trim
import kotlin.to
import kotlin.toString

class AddRecipeActivity : AppCompatActivity() {

    private var edtRecipeName: EditText? = null
    private var edtDescription: EditText? = null
    private var edtCookTime: EditText? = null
    private val db = FirebaseFirestore.getInstance()
    private var isEditMode = false
    private var originalName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Ánh xạ an toàn
        edtRecipeName = findViewById(R.id.edtRecipeName)
        edtDescription = findViewById(R.id.edtDescription)
        edtCookTime = findViewById(R.id.edtCookTime)
        
        val btnPublish = findViewById<Button>(R.id.btnPublish)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        // Kiểm tra chế độ Sửa
        val mode = intent.getStringExtra("MODE")
        if (mode == "EDIT") {
            isEditMode = true
            originalName = intent.getStringExtra("RECIPE_NAME")
            edtRecipeName?.setText(originalName)
            btnPublish?.text = "Cập nhật"
        }

        btnClose?.setOnClickListener { finish() }

        btnPublish?.setOnClickListener {
            val name = edtRecipeName?.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên món!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val recipeData = hashMapOf(
                "name" to name,
                "description" to edtDescription?.text.toString(),
                "cookTime" to edtCookTime?.text.toString(),
                "updatedAt" to System.currentTimeMillis()
            )

            val docRef = if (isEditMode && originalName != null) {
                db.collection("recipes").document(originalName!!)
            } else {
                db.collection("recipes").document(name)
            }

            docRef.set(recipeData)
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Đã lưu thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "❌ Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}