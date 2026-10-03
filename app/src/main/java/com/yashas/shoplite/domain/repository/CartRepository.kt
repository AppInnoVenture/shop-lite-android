package com.yashas.shoplite.domain.repository

import com.yashas.shoplite.domain.model.CartItem
import com.yashas.shoplite.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCart(): Flow<List<CartItem>>
    suspend fun addToCart(product: Product, quantity: Int = 1): Result<Unit>
    suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit>
    suspend fun removeFromCart(productId: String): Result<Unit>
    suspend fun clearCart(): Result<Unit>
}
