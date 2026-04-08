package com.example.demo.models

data class Recipe(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val author: String = "Admin",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val createdAt: Long = 0
)