package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.demo.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class changePasswordActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        //Quay lại
        val btnBack = findViewById<ImageButton>(R.id.btnBackChangePass)
        btnBack.setOnClickListener {
            finish()
        }

        //Đổi pass
        val edtCurrentPassword = findViewById<EditText>(R.id.edtCurrentPassword)
        val edtNewPassword = findViewById<EditText>(R.id.edtNewPassword)
        val edtConfirmNewPassword = findViewById<EditText>(R.id.edtConfirmNewPassword)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        btnChangePassword.setOnClickListener {
            val currentPass = edtCurrentPassword.text.toString().trim()
            val newPass = edtNewPassword.text.toString().trim()
            val confirmPass = edtConfirmNewPassword.text.toString().trim()

            // Kiểm tra nhập đầy đủ
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra độ dài mật khẩu mới
            if (newPass.length < 6) {
                Toast.makeText(this, "Mật khẩu mới phải ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra xác nhận mật khẩu
            if (newPass != confirmPass) {
                Toast.makeText(this, "Mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            changePassword(currentPass, newPass)
        }
    }
    private fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser

        if (user == null || user.email.isNullOrEmpty()) {
            Toast.makeText(this, "Không tìm thấy tài khoản. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show()
            return
        }

        // Bước 1: Re-authenticate (xác thực lại) bằng mật khẩu hiện tại
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Bước 2: Cập nhật mật khẩu mới
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show()
                        finish()   // Quay lại trang trước
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi khi đổi mật khẩu: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Mật khẩu hiện tại không đúng!", Toast.LENGTH_LONG).show()
            }
    }
}