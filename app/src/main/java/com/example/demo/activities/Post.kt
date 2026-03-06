package com.example.demo.activities

import android.media.Image

data class Post(
    val imgAvatar: Int,
    val name: String,
    val content: String,
    var likes: Int,
    val image: Int
)