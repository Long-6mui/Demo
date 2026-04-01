package com.example.demo.activities


data class Comment(
    var id: String = "",
    var postId: String = "",
    var userId: String = "",
    var content: String = "",
    var image: String = ""
)