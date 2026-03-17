package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth     = FirebaseAuth.getInstance()
        dbHelper = DatabaseHelper(this)

        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        val edtEmail    = findViewById<EditText>(R.id.edtEmail)
        val edtPass     = findViewById<EditText>(R.id.edtPass)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val txtGoLogin  = findViewById<TextView>(R.id.txtGoLogin)

        btnRegister.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val email    = edtEmail.text.toString().trim()
            val pass     = edtPass.text.toString().trim()

            // Kiểm tra không để trống
            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu đủ dài
            if (pass.length < 6) {
                Toast.makeText(this, "Mật khẩu phải ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tạo tài khoản Firebase
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user!!

                    // ✅ Lưu vào SQLite
                    dbHelper.saveUser(
                        userID = user.uid,
                        name   = username,
                        email  = email
                    )

                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    // Email đã tồn tại hoặc lỗi khác
                    Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        txtGoLogin.setOnClickListener { finish() }
    }
}