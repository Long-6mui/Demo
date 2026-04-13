package com.example.demo.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    var id: String = "",
    val userId: String = "",
    val imgAvatar: Int = 0,
    val name: String = "",
    val hoten: String = "",
    var content: String = "",
    // Chuyển đổi từ Int sang List để quản lý danh sách UID
    var likedBy: MutableList<String> = mutableListOf(),
    var image: String? = null,
    var imageUrl: String? = null,

    @ServerTimestamp
    var timestamp: Timestamp? = null


) {
    // Constructor mặc định bắt buộc phải có đủ các tham số khớp với bên trên
    constructor() : this("", "", 0, "", "", "", mutableListOf(), null, null, null)

    // Thuận tiện để lấy số lượng like hiển thị lên UI
    val likesCount: Int
        get() = likedBy.size
}