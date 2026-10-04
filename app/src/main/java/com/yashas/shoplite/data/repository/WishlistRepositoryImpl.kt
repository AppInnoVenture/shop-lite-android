package com.yashas.shoplite.data.repository

import com.yashas.shoplite.data.local.dao.WishlistDao
import com.yashas.shoplite.data.local.entity.WishlistEntity
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.WishlistRepository
import com.yashas.shoplite.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WishlistRepositoryImpl @Inject constructor(
    private val dao: WishlistDao,
    private val productRepository: ProductRepository
) : WishlistRepository {

    override fun getWishlistIds(): Flow<List<String>> {
        return dao.getWishlistItems().map { entities ->
            entities.map { it.productId }
        }
    }

    override fun getWishlistProducts(): Flow<List<Product>> {
        return dao.getWishlistItems().map { entities ->
            val products = mutableListOf<Product>()
            for ((productId) in entities) {
                // Fetch product from local cache (already populated by ProductRepository)
                val result = productRepository.getProductById(productId)
                result.getOrNull()?.let { products.add(it) }
            }
            products
        }
    }

    override suspend fun toggleWishlist(product: Product): Result<Boolean> {
        return try {
            val existing = dao.getWishlistItemById(product.id)
            if (existing != null) {
                dao.removeWishlistItem(existing)
                Result.success(false) // Removed
            } else {
                dao.insertWishlistItem(WishlistEntity(product.id))
                Result.success(true) // Added
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearWishlist(): Result<Unit> {
        return try {
            dao.clearWishlist()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
