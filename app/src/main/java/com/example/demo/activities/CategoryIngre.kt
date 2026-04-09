package com.example.demo.activities

import com.example.demo.models.Ingredient

data class CategoryIngre(
    val cateinID: String = "",
    val cateName: String = "",
    var isExpanded: Boolean = false,
    val ingredients: MutableList<Ingredient> = mutableListOf()
)