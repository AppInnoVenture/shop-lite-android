package com.yashas.shoplite.presentation.cart

import com.yashas.shoplite.domain.model.CartItem

data class CartState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val deliveryFee: Int = 10,
    val error: String? = null
) {
    val subtotal: Int get() = items.sumOf { it.price * it.quantity }
    val total: Int get() = if (items.isEmpty()) 0 else subtotal + deliveryFee
}
