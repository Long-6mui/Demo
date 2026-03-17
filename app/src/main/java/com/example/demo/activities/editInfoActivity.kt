package com.example.demo.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar

class editInfoActivity : AppCompatActivity() {
    private lateinit var imgAvatar: ImageView
    private var imageUri: Uri? = null

    lateinit var edtName: EditText
    lateinit var edtID: EditText
    lateinit var edtBirth: EditText
    lateinit var edtBio: EditText
    lateinit var radioMale: RadioButton
    lateinit var radioFemale: RadioButton
    lateinit var btnUpdate: Button
    lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_info)

        //chọn avatar
        imgAvatar = findViewById(R.id.imgAvatar)
        imgAvatar.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK)
            gallery.type = "image/*"
            startActivityForResult(gallery, 100)
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

        edtName = findViewById(R.id.edtName)
        edtID = findViewById(R.id.edtID)
        edtBirth = findViewById(R.id.edtBirthDay)
        edtBio = findViewById(R.id.edtBio)
        radioMale = findViewById(R.id.radioMale)
        radioFemale = findViewById(R.id.radioFemale)
        btnUpdate = findViewById(R.id.btnUpdate)
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        //Hiện thông tin
        val userUID = auth.currentUser
        if (userUID != null) {
            val userID = userUID.uid
            database.collection("Users")
                .document(userID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val avatarUrl = document.getString("avatar")
                        Glide.with(this)
                            .load(avatarUrl)
                            .into(imgAvatar)
                        val name = document.getString("name")
                        val id = document.getString("id")
                        val birth = document.getString("birth")
                        val gender = document.getString("gender")
                        val bio = document.getString("bio")
                        edtName.setText(name)
                        edtID.setText(id)
                        edtBirth.setText(birth)
                        edtBio.setText(bio)
                        if (gender == "Nam") {
                            radioMale.isChecked = true
                        } else {
                            radioFemale.isChecked = true
                        }
                    }
                }
        }
        //nút cập nhật thông tin
        btnUpdate.setOnClickListener {
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val name = edtName.text.toString()
                val id = edtID.text.toString()
                val birth = edtBirth.text.toString()
                val bio = edtBio.text.toString()
                val gender = if (radioMale.isChecked) {
                    "Nam"
                } else {
                    "Nữ"
                }
                if (name.isEmpty()) {
                    edtName.error = "Nhập họ tên"
                    return@setOnClickListener
                }
                if (id.isEmpty()) {
                    edtID.error = "Nhập ID"
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
                                saveUserData(userId, name, id, birth, gender, bio, avatarUrl)
                            }
                        }
                } else {
                    saveUserData(userId, name, id, birth, gender, bio, null)
                }
            }
        }
    }
    fun saveUserData(
        userId: String,
        name: String,
        id: String,
        birth: String,
        gender: String,
        bio: String,
        avatarUrl: String?
    ) {
        val userData = hashMapOf<String, Any>(
            "name" to name,
            "id" to id,
            "birth" to birth,
            "gender" to gender,
            "bio" to bio
        )
        if (avatarUrl != null) {
            userData["avatar"] = avatarUrl
        }
        database.collection("Users")
            .document(userId)
            .update(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data
            imgAvatar.setImageURI(imageUri)
        }
    }
}