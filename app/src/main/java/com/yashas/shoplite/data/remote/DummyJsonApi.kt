package com.yashas.shoplite.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DummyJsonApi {
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 100,
        @Query("skip") skip: Int = 0
    ): ProductResponseDto

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductDto

    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): ProductResponseDto
    
    @GET("products/category-list")
    suspend fun getCategories(): List<String>
    
    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): ProductResponseDto
}

data class ProductResponseDto(
    val products: List<ProductDto>
)

data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val category: String,
    val thumbnail: String,
    val images: List<String>?,
    val brand: String?,
    val sku: String?,
    val warrantyInformation: String?,
    val shippingInformation: String?,
    val returnPolicy: String?,
    val dimensions: DimensionsDto?,
    val reviews: List<ReviewDto>?
)

data class DimensionsDto(
    val width: Double,
    val height: Double,
    val depth: Double
)

data class ReviewDto(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String
)
