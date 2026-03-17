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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth     = FirebaseAuth.getInstance()
        dbHelper = DatabaseHelper(this)

        // ✅ Đã đăng nhập rồi → vào thẳng MainActivity
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        val edtUser       = findViewById<EditText>(R.id.edtUser)
        val edtPass       = findViewById<EditText>(R.id.edtPass)
        val btnLogin      = findViewById<Button>(R.id.btnLogin)
        val txtGoRegister = findViewById<TextView>(R.id.txtGoRegister)

        btnLogin.setOnClickListener {
            val email = edtUser.text.toString().trim()
            val pass  = edtPass.text.toString().trim()

            // Kiểm tra không để trống
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Đăng nhập Firebase
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user!!

                    // Lưu vào SQLite nếu chưa có
                    if (!dbHelper.isUserExists(user.uid)) {
                        dbHelper.saveUser(
                            userID = user.uid,
                            name   = email.substringBefore("@"),
                            email  = email
                        )
                    }

                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    goToMain()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                }
        }

        txtGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}