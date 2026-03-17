package com.example.demo.models

class CategoryIngre(val cateinID: String = "",
                    val cateName: String = "",
                    val ingredients: MutableList<Ingredient> = mutableListOf(),
                    var isExpanded: Boolean = false) {
}