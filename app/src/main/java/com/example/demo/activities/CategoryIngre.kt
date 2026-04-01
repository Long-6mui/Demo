package com.example.demo.activities

data class CategoryIngre(
    val cateinID: String = "",
    val cateName: String = "",
    var isExpanded: Boolean = false,
    val ingredients: MutableList<Ingredient> = mutableListOf()
)