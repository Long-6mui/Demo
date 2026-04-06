package com.example.demo.activities

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
}