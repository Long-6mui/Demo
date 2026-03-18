package com.example.demo.activities

data class Comment(
    var id: Int = 0,
    var postId: String = "",
    var userId: String = "",
    var content: String = "",
    var image: String = ""
)