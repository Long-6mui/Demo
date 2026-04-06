package com.example.demo.activities

import android.media.Image

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
    var imageUrl: String? = null
) {
    // Thuận tiện để lấy số lượng like hiển thị lên UI
    val likesCount: Int
        get() = likedBy.size
}