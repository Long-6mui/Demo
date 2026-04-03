package com.example.demo

data class Recipe(
    val id: String = "",
    val name: String = "",
    val image: String = "", // Đổi sang String để chứa URL từ Firebase
    val author: String = "Admin",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val createdAt: Long = 0
)