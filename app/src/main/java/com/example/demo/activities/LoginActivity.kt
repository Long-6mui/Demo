package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val edtUser       = findViewById<EditText>(R.id.edtUser)
        val edtPass       = findViewById<EditText>(R.id.edtPass)
        val btnLogin      = findViewById<Button>(R.id.btnLogin)
        val txtGoRegister = findViewById<TextView>(R.id.txtGoRegister)

        // Nếu đã login rồi → auto vào app
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("AUTO_LOGIN", "UID: ${currentUser.uid}")
            checkRoleAndNavigate(currentUser.uid)
        }

        btnLogin.setOnClickListener {
            val email = edtUser.text.toString().trim()
            val pass  = edtPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Đang đăng nhập..."

            Log.d("LOGIN_ATTEMPT", "Email: $email")

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->

                    // reset UI
                    btnLogin.isEnabled = true
                    btnLogin.text = "Đăng nhập"

                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        Log.d("LOGIN_SUCCESS", "UID: $uid")

                        if (uid != null) {
                            checkRoleAndNavigate(uid)
                        }
                    } else {
                        Log.e("LOGIN_ERROR", task.exception.toString())

                        Toast.makeText(
                            this,
                            "Login thất bại: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        txtGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkRoleAndNavigate(userID: String) {
        Log.d("CHECK_ROLE", "Checking role for UID: $userID")

        db.collection("Users").document(userID)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {
                    val role = doc.getString("role") ?: "user"
                    Log.d("ROLE_RESULT", "Role: $role")

                    val intent = if (role == "admin") {
                        Intent(this, AdminActivity::class.java)
                    } else {
                        Intent(this, MainActivity::class.java)
                    }

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } else {
                    Log.e("ROLE_ERROR", "User document NOT FOUND in Firestore")

                    Toast.makeText(
                        this,
                        "Không tìm thấy thông tin user!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener {
                Log.e("FIRESTORE_ERROR", it.message.toString())

                Toast.makeText(
                    this,
                    "Lỗi Firestore: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}