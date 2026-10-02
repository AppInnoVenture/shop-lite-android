package com.yashas.shoplite.domain.repository

import com.yashas.shoplite.domain.model.CartItem
import com.yashas.shoplite.domain.model.Product

interface CartRepository {
    suspend fun getCart(): Flow<List<CartItem>>
    suspend fun addToCart(userId: String, product: Product, quantity: Int = 1): Result<Unit>
    suspend fun updateQuantity(userId: String, productId: String, quantity: Int): Result<Unit>
    suspend fun removeFromCart(userId: String, productId: String): Result<Unit>
    suspend fun clearCart(userId: String): Result<Unit>
}
