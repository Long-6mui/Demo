package com.example.demo.activities

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
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
            val btnBack = findViewById<ImageButton>(R.id.btnBack)
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

                // 1. Lấy dữ liệu từ Firestore (ưu tiên)
                database.collection("Users")
                    .document(userID)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Lấy dữ liệu từ Firestore
                            val name = document.getString("name") ?: ""
                            val hoten = document.getString("hoten") ?: ""
                            val email = document.getString("email") ?: ""
                            val birth = document.getString("birth") ?: ""
                            val gender = document.getString("gender") ?: ""
                            val avatarUrl = document.getString("avatar") ?: ""

                            // Hiển thị lên UI
                            edtHoTen.setText(hoten.ifEmpty { name })   // Ưu tiên họ tên, nếu rỗng thì lấy username
                            edtUserName.setText(name)
                            edtBirth.setText(birth)
                            edtEmail.setText(user.email)               // Email lấy từ FirebaseAuth

                            if (gender == "Nam") {
                                radioMale.isChecked = true
                            } else if (gender == "Nữ") {
                                radioFemale.isChecked = true
                            }

                            // Load avatar
                            if (avatarUrl.isNotEmpty()) {
                                Glide.with(this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.avtque)
                                    .into(imgAvatar)
                            } else {
                                imgAvatar.setImageResource(R.drawable.avtque)
                            }

                            // Đồng bộ lại vào SQLite để đồng bộ dữ liệu
                            dbHelper.updateUserByUID(
                                userID,
                                name,
                                hoten,
                                email,
                                birth,
                                gender,
                                avatarUrl
                            )

                        } else {
                            // Trường hợp Firestore chưa có document → fallback về SQLite
                            loadFromSQLite(userID)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi tải thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
                        // Fallback về SQLite khi lỗi mạng hoặc Firestore
                        loadFromSQLite(userID)
                    }
            } else {
                Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
                finish()
            }
            //nút cập nhật thông tin
            btnUpdate.setOnClickListener {
                val user = auth.currentUser
                if (user != null) {
                    val userId = user.uid
                    val name = edtUserName.text.toString()
                    val hoten = edtHoTen.text.toString()
                    val birth = edtBirth.text.toString()
                    val gender = if (radioMale.isChecked) "Nam" else "Nữ"
                    val oldAvatar = dbHelper.getUserByUID(userId)?.avatar ?: ""
                    if (hoten.isEmpty()) {
                        edtHoTen.error = "Vui lòng nhập họ tên"
                        return@setOnClickListener
                    }
                    if (name.isEmpty()) {
                        edtUserName.error = "Vui lòng nhập username"
                        return@setOnClickListener
                    }
                    if (imageUri != null) {
                        uploadAvatarToCloudinary(imageUri!!) { avatarUrl ->
                            saveUserData(userId, name, hoten, birth, gender, avatarUrl)
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
        val user = auth.currentUser
        val email = user?.email ?: ""

        val userData = hashMapOf<String, Any>(
            "name" to name,
            "hoten" to hoten,
            "birth" to birth,
            "gender" to gender,
            "email" to email
        )

        if (!avatarUrl.isNullOrEmpty()) {
            userData["avatar"] = avatarUrl
        }

        database.collection("Users")
            .document(userId)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                val finalAvatar = avatarUrl ?: dbHelper.getUserByUID(userId)?.avatar ?: ""

                // ✅ SỬA Ở ĐÂY: Cập nhật đầy đủ vào SQLite (bao gồm email)
                dbHelper.updateUserByUID(
                    userId = userId,
                    name = name,
                    hoten = hoten,
                    email = email,
                    birthday = birth,
                    gender = gender,
                    avatar = finalAvatar
                )

                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFromSQLite(userID: String) {
        val userData = dbHelper.getUserByUID(userID)
        if (userData != null) {
            edtHoTen.setText(if (userData.hoten.isEmpty()) userData.name else userData.hoten)
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
    private fun uploadAvatarToCloudinary(uri: Uri, onSuccess: (String) -> Unit) {
        Toast.makeText(this, "Đang upload avatar...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(uri)
            .option("folder", "avatars")           // lưu vào thư mục avatars
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload avatar started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val avatarUrl = resultData?.get("secure_url")?.toString() ?: ""
                    Log.d("Cloudinary", "Upload avatar thành công: $avatarUrl")

                    if (avatarUrl.isNotEmpty()) {
                        // Hiển thị ngay avatar mới
                        Glide.with(this@editInfoActivity)
                            .load(avatarUrl)
                            .into(imgAvatar)

                        onSuccess(avatarUrl)
                    } else {
                        Toast.makeText(this@editInfoActivity, "Upload thành công nhưng không lấy được URL", Toast.LENGTH_SHORT).show()
                        onSuccess("")
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload avatar lỗi: ${error?.description}")
                    Toast.makeText(this@editInfoActivity, "Upload avatar thất bại", Toast.LENGTH_SHORT).show()
                    onSuccess("")   // vẫn tiếp tục lưu thông tin khác
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}