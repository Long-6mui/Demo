package com.example.demo.activities

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar

class editInfoActivity : AppCompatActivity() {
    private lateinit var imgAvatar: ImageView
    private var imageUri: Uri? = null

    lateinit var edtUserName: EditText
    lateinit var edtHoTen: EditText
    lateinit var edtEmail: EditText
    lateinit var edtBirth: EditText
    lateinit var radioMale: RadioButton
    lateinit var radioFemale: RadioButton
    lateinit var btnUpdate: Button
    lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    lateinit var storageRef: StorageReference
    lateinit var dbHelper: DatabaseHelper

    val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            // hiển thị ngay avatar
            imgAvatar.setImageURI(uri)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_info)

        dbHelper = DatabaseHelper(this)
        //chọn avatar
        imgAvatar = findViewById(R.id.imgAvatar)
        imgAvatar.setOnClickListener {
            imagePicker.launch("image/*")
        }
        //chọn ngày sinh
        edtBirth = findViewById(R.id.edtBirthDay)
        edtBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                    edtBirth.setText(date)
                },
                year, month, day
            )
            datePicker.show()
        }
        //Quay lại
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        edtHoTen = findViewById(R.id.edtTen)
        edtUserName = findViewById(R.id.edtUserName)
        edtEmail = findViewById(R.id.edtEmail)
        edtBirth = findViewById(R.id.edtBirthDay)
        radioMale = findViewById(R.id.radioMale)
        radioFemale = findViewById(R.id.radioFemale)
        btnUpdate = findViewById(R.id.btnUpdate)
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        //Hiện thông tin
        val user = auth.currentUser
        if (user != null) {
            val userID = user.uid
            val userData = dbHelper.getUserByUID(userID)
            if (userData != null) {

                if (userData.hoten.isEmpty()) {
                    edtHoTen.setText(userData.name)
                } else {
                    edtHoTen.setText(userData.hoten)
                }
                edtUserName.setText(userData.name)
                edtBirth.setText(userData.birthday)
                edtEmail.setText(userData.email)
                if (userData.gender == "Nam") {
                    radioMale.isChecked = true
                } else {
                    radioFemale.isChecked = true
                }
                if (userData.avatar.isEmpty()) {
                    imgAvatar.setImageResource(R.drawable.avtque)
                } else {
                    Glide.with(this)
                        .load(userData.avatar)
                        .into(imgAvatar)
                }
            }
        }
        //nút cập nhật thông tin
        btnUpdate.setOnClickListener {
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val name = edtUserName.text.toString()
                val hoten = edtHoTen.text.toString()
                val birth = edtBirth.text.toString()
                val gender = if (radioMale.isChecked) {
                    "Nam"
                } else {
                    "Nữ"
                }
                val oldAvatar = dbHelper.getUserByUID(userId)?.avatar ?: ""
                if (hoten.isEmpty()) {
                    edtHoTen.error = "Nhập họ tên"
                    return@setOnClickListener
                }
                if (name.isEmpty()) {
                    edtUserName.error = "Nhập username"
                    return@setOnClickListener
                }
                if (birth.isEmpty()) {
                    edtBirth.error = "Nhập ngày sinh"
                    return@setOnClickListener
                }
                if (imageUri != null) {
                    val fileRef = storageRef.child("avatars/$userId.jpg")
                    fileRef.putFile(imageUri!!)
                        .addOnSuccessListener {
                            fileRef.downloadUrl.addOnSuccessListener { uri ->
                                val avatarUrl = uri.toString()
                                Glide.with(this)
                                    .load(avatarUrl)
                                    .into(imgAvatar)
                                saveUserData(userId, name, hoten, birth, gender, avatarUrl)
                            }
                        }
                } else {
                    saveUserData(userId, name, hoten, birth, gender, oldAvatar)
                }
            }
        }
    }
    fun saveUserData(
        userId: String,
        name: String,
        hoten: String,
        birth: String,
        gender: String,
        avatarUrl: String?
    ) {
        val userData = hashMapOf<String, Any>(
            "name" to name,
            "hoten" to hoten,
            "birth" to birth,
            "gender" to gender,
        )
        if (avatarUrl != null) {
            userData["avatar"] = avatarUrl
        }
        database.collection("Users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                val finalAvatar = avatarUrl
                    ?: dbHelper.getUserByUID(userId)?.avatar
                    ?: ""
                // ✅ update SQLite
                dbHelper.updateUserByUID(
                    userId,
                    name,
                    hoten,
                    birth,
                    gender,
                    finalAvatar
                )
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show()
            }
    }
}