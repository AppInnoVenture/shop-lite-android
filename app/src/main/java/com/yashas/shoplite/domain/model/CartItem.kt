package com.yashas.shoplite.domain.model

data class CartItem(
    val productId: String,
    val name: String,
    val price: Int,
    val imageUrl: String,
    val quantity: Int
)
