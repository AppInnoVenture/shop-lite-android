package com.yashas.shoplite.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DummyJsonApi {
    @GET("products")
    suspend fun getProducts(): ProductResponseDto

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductDto

    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): ProductResponseDto
}

data class ProductResponseDto(
    val products: List<ProductDto>
)

data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val stock: Int,
    val category: String,
    val thumbnail: String
)