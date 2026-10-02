package com.yashas.shoplite.domain.model

data class Product(
    val id: String,
    val category: String,
    val name: String,
    val rating: Double,
    val price: Int,
    val imageUrl: String,
    val description: String,
    val stock: Int
)
