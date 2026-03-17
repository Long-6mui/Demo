package com.example.demo.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.activities.ProfileActivity
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R

import java.util.Calendar

class editInfoActivity : AppCompatActivity() {
    private lateinit var imgAvatar: ImageView
    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_info)

        //chọn avatar
        imgAvatar = findViewById(R.id.imgAvatar)
        imgAvatar.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK)
            gallery.type = "image/*"
            startActivityForResult(gallery, PICK_IMAGE)
        }
        //chọn ngày sinh
        val edtBirthDay = findViewById<EditText>(R.id.edtBirthDay)
        edtBirthDay.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(
                this,
                { _, y, m, d ->
                    edtBirthDay.setText("$d/${m + 1}/$y")
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
        val db = DatabaseHelper(this)
    }
}