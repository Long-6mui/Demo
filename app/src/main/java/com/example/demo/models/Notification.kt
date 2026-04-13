package com.example.demo.models

data class Notification(
    var id: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val postId: String = "",
    val type: String = "", // "like" or "comment"
    val content: String = "",
    val timestamp: Long = 0,
    var seen: Boolean = false
) {
    constructor() : this("", "", "", "", "", "", "", 0, false)
}