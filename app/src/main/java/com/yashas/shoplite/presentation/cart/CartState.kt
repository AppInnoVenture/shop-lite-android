package com.yashas.shoplite.presentation.cart

import com.yashas.shoplite.domain.model.CartItem

data class CartState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val isLoading: Boolean = true
)
