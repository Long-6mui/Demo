package com.example.demo.models

import com.example.demo.R

data class Dish(
    val id: Int = 0,
    val name: String,
    val imgRes: Int = R.drawable.choco,
    val info: String,
    val author: String = "Admin"
)