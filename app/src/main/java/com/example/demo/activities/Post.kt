package com.example.demo.activities

import android.media.Image

data class Post(
    var id: String = "",
    val userId: String = "",
    val imgAvatar: Int = 0,
    val name: String = "",
    val hoten: String = "",
    var content: String = "",
    var likes: Int = 0,
    var image: String? = null,
    var imageUrl: String? = null
)