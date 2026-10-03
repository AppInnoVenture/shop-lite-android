package com.yashas.shoplite.domain.repository

import com.yashas.shoplite.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavoritesIds(): Flow<List<String>>
    fun getFavoriteProducts(): Flow<List<Product>>
    suspend fun toggleFavorite(product: Product): Result<Boolean> // Returns true if added, false if removed
}
