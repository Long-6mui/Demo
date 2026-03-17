package com.example.demo.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R

class FeedbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val btnBack = findViewById<ImageButton>(R.id.btnBackFeedback)
        val btnSend = findViewById<Button>(R.id.btnSendFeedback)
        val edtFeedback = findViewById<EditText>(R.id.edtFeedback)

        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {

            val text = edtFeedback.text.toString()

            if (text.isEmpty()) {

                Toast.makeText(this, "Vui lòng nhập góp ý", Toast.LENGTH_SHORT).show()

            } else {

                Toast.makeText(this, "Cảm ơn bạn đã góp ý ❤️", Toast.LENGTH_SHORT).show()
                edtFeedback.setText("")

            }

        }

    }
}