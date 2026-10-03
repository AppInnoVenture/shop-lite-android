package com.yashas.shoplite.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val quantity: Int
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val category: String,
    val name: String,
    val rating: Double,
    val price: Double,
    val imageUrl: String,
    val imagesJson: String,
    val description: String,
    val stock: Int,
    val brand: String,
    val sku: String,
    val warrantyInformation: String,
    val shippingInformation: String,
    val returnPolicy: String,
    val dimensionsJson: String,
    val reviewsJson: String,
    val lastUpdated: Long
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: String
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :id LIMIT 1")
    suspend fun getCartItemById(id: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :id")
    suspend fun deleteItem(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>
    
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)
    
    @Query("DELETE FROM products")
    suspend fun clearAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getFavorites(): Flow<List<FavoriteEntity>>
    
    @Query("SELECT * FROM favorites WHERE productId = :id LIMIT 1")
    suspend fun getFavoriteById(id: String): FavoriteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)
    
    @Delete
    suspend fun removeFavorite(favorite: FavoriteEntity)
}

@Database(entities = [CartItemEntity::class, ProductEntity::class, FavoriteEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val cartDao: CartDao
    abstract val productDao: ProductDao
    abstract val favoriteDao: FavoriteDao
}
