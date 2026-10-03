package com.yashas.shoplite.domain.repository

import com.yashas.shoplite.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(forceRefresh: Boolean = false): Result<List<Product>>
    suspend fun getProductById(id: String): Result<Product>
    suspend fun searchProducts(query: String): Result<List<Product>>
    suspend fun getCategories(): Result<List<String>>
    suspend fun getProductsByCategory(category: String): Result<List<Product>>
}
