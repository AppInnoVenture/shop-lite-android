package com.yashas.shoplite.data.repository

import com.yashas.shoplite.data.remote.DummyJsonApi
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: DummyJsonApi
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = api.getProducts()
            Result.success(response.products.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProduct(productId: String): Result<Product> {
        return try {
            val response = api.getProductById(productId)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val response = api.searchProducts(query)
            Result.success(response.products.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Mapper extension function
fun com.yashas.shoplite.data.remote.ProductDto.toDomain(): Product {
    return Product(
        id = id.toString(),
        category = category,
        name = title,
        rating = rating,
        price = price.toInt(),
        imageUrl = thumbnail,
        description = description,
        stock = stock
    )
}