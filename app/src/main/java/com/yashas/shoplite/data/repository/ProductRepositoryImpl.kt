package com.yashas.shoplite.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yashas.shoplite.data.local.ProductDao
import com.yashas.shoplite.data.local.ProductEntity
import com.yashas.shoplite.data.remote.DummyJsonApi
import com.yashas.shoplite.data.remote.ProductDto
import com.yashas.shoplite.domain.model.Dimensions
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.model.Review
import com.yashas.shoplite.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: DummyJsonApi,
    private val productDao: ProductDao,
    private val gson: Gson
) : ProductRepository {

    private val CACHE_EXPIRY_MS = 10 * 60 * 1000L // 10 minutes

    override fun getProductsFlow(): Flow<List<Product>> {
        return productDao.getAllProductsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncProducts(forceRefresh: Boolean): Result<Unit> {
        return try {
            val cachedEntities = productDao.getAllProducts()
            val currentTime = System.currentTimeMillis()
            
            // Check if cache is valid
            if (!forceRefresh && cachedEntities.isNotEmpty() && (currentTime - cachedEntities.first().lastUpdated < CACHE_EXPIRY_MS)) {
                return Result.success(Unit)
            }

            // Fetch from network
            val response = api.getProducts()
            val entities = response.products.map { it.toEntity(currentTime) }
            
            // Update cache
            productDao.clearAll()
            productDao.insertAll(entities)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProducts(forceRefresh: Boolean): Result<List<Product>> {
        return try {
            val cachedEntities = productDao.getAllProducts()
            val currentTime = System.currentTimeMillis()
            
            // Check if cache is valid
            if (!forceRefresh && cachedEntities.isNotEmpty() && (currentTime - cachedEntities.first().lastUpdated < CACHE_EXPIRY_MS)) {
                return Result.success(cachedEntities.map { it.toDomain() })
            }

            // Fetch from network
            val response = api.getProducts()
            val entities = response.products.map { it.toEntity(currentTime) }
            
            // Update cache
            productDao.clearAll()
            productDao.insertAll(entities)
            
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            // Fallback to cache if network fails
            val cachedEntities = productDao.getAllProducts()
            if (cachedEntities.isNotEmpty()) {
                Result.success(cachedEntities.map { it.toDomain() })
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getProductById(id: String): Result<Product> {
        return try {
            val cachedProduct = productDao.getProductById(id)
            if (cachedProduct != null) {
                Result.success(cachedProduct.toDomain())
            } else {
                val dto = api.getProductById(id)
                Result.success(dto.toEntity(System.currentTimeMillis()).toDomain())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val response = api.searchProducts(query)
            Result.success(response.products.map { it.toEntity(System.currentTimeMillis()).toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCategories(): Result<List<String>> {
        return try {
            val categories = api.getCategories()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        return try {
            val response = api.getProductsByCategory(category)
            Result.success(response.products.map { it.toEntity(System.currentTimeMillis()).toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ProductDto.toEntity(timestamp: Long): ProductEntity {
        return ProductEntity(
            id = id.toString(),
            category = category,
            name = title,
            rating = rating,
            price = price,
            discountPercentage = discountPercentage,
            imageUrl = thumbnail,
            imagesJson = gson.toJson(images ?: emptyList<String>()),
            description = description,
            stock = stock,
            brand = brand ?: "",
            sku = sku ?: "",
            warrantyInformation = warrantyInformation ?: "",
            shippingInformation = shippingInformation ?: "",
            returnPolicy = returnPolicy ?: "",
            dimensionsJson = gson.toJson(dimensions ?: Dimensions(0.0, 0.0, 0.0)),
            reviewsJson = gson.toJson(reviews ?: emptyList<Review>()),
            lastUpdated = timestamp
        )
    }

    private fun ProductEntity.toDomain(): Product {
        val stringListType = object : TypeToken<List<String>>() {}.type
        val reviewListType = object : TypeToken<List<Review>>() {}.type
        
        return Product(
            id = id,
            category = category,
            name = name,
            rating = rating,
            price = price,
            discountPercentage = discountPercentage,
            imageUrl = imageUrl,
            images = try { gson.fromJson(imagesJson, stringListType) } catch (e: Exception) { emptyList() },
            description = description,
            stock = stock,
            brand = brand,
            sku = sku,
            warrantyInformation = warrantyInformation,
            shippingInformation = shippingInformation,
            returnPolicy = returnPolicy,
            dimensions = try { gson.fromJson(dimensionsJson, Dimensions::class.java) } catch (e: Exception) { Dimensions(0.0, 0.0, 0.0) },
            reviews = try { gson.fromJson(reviewsJson, reviewListType) } catch (e: Exception) { emptyList() }
        )
    }
}
