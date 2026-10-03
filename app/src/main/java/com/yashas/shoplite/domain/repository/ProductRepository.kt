package com.yashas.shoplite.domain.repository

import com.yashas.shoplite.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsFlow(): Flow<List<Product>>
    suspend fun isCacheValid(): Boolean
    suspend fun syncProducts(forceRefresh: Boolean = false): Result<Unit>
    suspend fun getProducts(forceRefresh: Boolean = false): Result<List<Product>>
    suspend fun getProductById(id: String): Result<Product>
    suspend fun searchProducts(query: String): Result<List<Product>>
    suspend fun getCategories(): Result<List<String>>
    suspend fun getProductsByCategory(category: String): Result<List<Product>>
}
