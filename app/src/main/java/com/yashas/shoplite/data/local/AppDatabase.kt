package com.yashas.shoplite.data.local

import androidx.room.*
import com.yashas.shoplite.domain.model.CartItem
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val price: Int,
    val imageUrl: String,
    val quantity: Int
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

@Database(entities = [CartItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val cartDao: CartDao
}

class CartRepositoryImpl @Inject constructor(
    private val dao: CartDao
) : CartRepository {

    // NOTE: Change your domain/repository/CartRepository.kt to return Flow<List<CartItem>> instead of Result for getCart()
    override fun getCart(): Flow<List<CartItem>> {
        return dao.getCartItems().map { entities ->
            entities.map { CartItem(it.productId, it.name, it.price, it.imageUrl, it.quantity) }
        }
    }

    override suspend fun addToCart(product: Product, quantity: Int): Result<Unit> {
        return try {
            val existingItem = dao.getCartItemById(product.id)
            if (existingItem != null) {
                dao.updateQuantity(product.id, existingItem.quantity + quantity)
            } else {
                dao.insertOrUpdate(CartItemEntity(product.id, product.name, product.price, product.imageUrl, quantity))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) dao.deleteItem(productId) else dao.updateQuantity(productId, quantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        return try {
            dao.deleteItem(productId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCart(): Result<Unit> {
        return try {
            dao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}