package com.yashas.shoplite.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogApiService {
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
