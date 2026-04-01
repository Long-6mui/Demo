package com.example.demo.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R
import com.google.firebase.firestore.FirebaseFirestore

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var edtRecipeName: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtCookTime: EditText
    private lateinit var layoutIngredients: LinearLayout
    private lateinit var layoutSteps: LinearLayout

    private var ingredientsList = mutableListOf<String>()
    private var stepsList = mutableListOf<String>()

    private lateinit var dbHelper: DatabaseHelper
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        dbHelper = DatabaseHelper(this)

        edtRecipeName = findViewById(R.id.edtRecipeName)
        edtDescription = findViewById(R.id.edtDescription)
        edtCookTime = findViewById(R.id.edtCookTime)
        layoutIngredients = findViewById(R.id.layoutIngredients)
        layoutSteps = findViewById(R.id.layoutSteps)

        val btnPublish = findViewById<Button>(R.id.btnPublish)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnAddIngredient = findViewById<TextView>(R.id.btnAddIngredient)
        val btnAddStep = findViewById<TextView>(R.id.btnAddStep)

        btnClose.setOnClickListener { finish() }

        btnAddIngredient.setOnClickListener {
            // Nhắc Admin chỉ nhập tên nguyên liệu, không nhập khối lượng
            showInputDialog("Thêm nguyên liệu", "Chỉ nhập tên (VD: Thịt bò)") { value ->
                // CHUẨN HÓA: Viết thường, xóa khoảng trắng thừa
                val cleanValue = value.lowercase().trim()
                ingredientsList.add(cleanValue)
                // Hiển thị cho Admin xem thì vẫn đẹp, nhưng lưu vào List là chữ sạch
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
            val desc = edtDescription.text.toString().trim()

            // Nối danh sách nguyên liệu thành chuỗi: "thịt bò, hành tây, tỏi"
            val ingredientsString = ingredientsList.joinToString(", ")

            if (name.isEmpty() || ingredientsList.isEmpty()) {
                Toast.makeText(this, "Nhập tên món và nguyên liệu đi Thịnh!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Lưu SQLite (Phục vụ tìm kiếm bên User)
            // Lưu ảnh rỗng "" vì Admin chưa làm phần up ảnh
            dbHelper.addRecipe(name, ingredientsString, "", desc)

            // 2. Lưu Firebase
            val recipeData = hashMapOf(
                "name" to name,
                "description" to desc,
                "ingredients" to ingredientsList,
                "steps" to stepsList,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("recipes").document(name).set(recipeData)
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Đã đăng món thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi Firebase rồiii", Toast.LENGTH_SHORT).show()
                }
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