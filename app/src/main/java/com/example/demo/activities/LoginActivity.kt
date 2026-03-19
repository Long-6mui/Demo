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
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//        FirebaseAuth.getInstance().signOut() tự động đăng xuất khi mở

        auth     = FirebaseAuth.getInstance()
        dbHelper = DatabaseHelper(this)

        // Đã đăng nhập → check role luôn
        if (auth.currentUser != null) {
            checkRoleAndNavigate(auth.currentUser!!.uid)
            return
        }

        val edtUser       = findViewById<EditText>(R.id.edtUser)
        val edtPass       = findViewById<EditText>(R.id.edtPass)
        val btnLogin      = findViewById<Button>(R.id.btnLogin)
        val txtGoRegister = findViewById<TextView>(R.id.txtGoRegister)

        btnLogin.setOnClickListener {
            val email = edtUser.text.toString().trim()
            val pass  = edtPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user!!

                    if (!dbHelper.isUserExists(user.uid)) {
                        dbHelper.saveUser(
                            userID = user.uid,
                            name   = email.substringBefore("@"),
                            email  = email
                        )
                    }

                    // Check role thay vì goToMain()
                    checkRoleAndNavigate(user.uid)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                }
        }

        txtGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkRoleAndNavigate(userID: String) {
        android.util.Log.d("LOGIN", "Checking uid: $userID")

        db.collection("Users").document(userID)
            .get()
            .addOnSuccessListener { doc ->
                android.util.Log.d("LOGIN", "exists: ${doc.exists()}, data: ${doc.data}")
                val role = doc.getString("role") ?: "user"
                android.util.Log.d("LOGIN", "role = $role")

                if (role == "admin") {
                    startActivity(Intent(this, AdminActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                android.util.Log.e("LOGIN", "Lỗi: ${it.message}")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}