package com.yashas.shoplite.domain.repository

import com.yashas.shoplite.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProduct(productId: String): Result<Product>
    suspend fun searchProducts(query: String): Result<List<Product>>
}
