package com.yashas.shoplite.domain.repository

import com.yashas.shoplite.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {
    fun getWishlistIds(): Flow<List<String>>
    fun getWishlistProducts(): Flow<List<Product>>
    suspend fun toggleWishlist(product: Product): Result<Boolean> // Returns true if added, false if removed
    suspend fun clearWishlist(): Result<Unit>
}
