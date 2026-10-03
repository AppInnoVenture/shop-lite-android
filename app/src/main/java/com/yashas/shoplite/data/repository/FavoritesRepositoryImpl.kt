package com.yashas.shoplite.data.repository

import com.yashas.shoplite.data.local.FavoriteDao
import com.yashas.shoplite.data.local.FavoriteEntity
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.FavoritesRepository
import com.yashas.shoplite.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
    private val productRepository: ProductRepository
) : FavoritesRepository {

    override fun getFavoritesIds(): Flow<List<String>> {
        return dao.getFavorites().map { entities ->
            entities.map { it.productId }
        }
    }

    override fun getFavoriteProducts(): Flow<List<Product>> {
        return dao.getFavorites().map { entities ->
            val products = mutableListOf<Product>()
            for (entity in entities) {
                // Fetch product from local cache (already populated by ProductRepository)
                val result = productRepository.getProductById(entity.productId)
                result.getOrNull()?.let { products.add(it) }
            }
            products
        }
    }

    override suspend fun toggleFavorite(product: Product): Result<Boolean> {
        return try {
            val existing = dao.getFavoriteById(product.id)
            if (existing != null) {
                dao.removeFavorite(existing)
                Result.success(false) // Removed
            } else {
                dao.insertFavorite(FavoriteEntity(product.id))
                Result.success(true) // Added
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
