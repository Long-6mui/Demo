package com.example.demo

data class Feedback(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    @field:JvmField
    var isInterested: Boolean = false,
    var adminReply: String = ""
)