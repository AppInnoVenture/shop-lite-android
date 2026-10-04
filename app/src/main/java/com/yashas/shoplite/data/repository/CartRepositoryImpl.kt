package com.yashas.shoplite.data.repository

import com.yashas.shoplite.data.local.dao.CartDao
import com.yashas.shoplite.data.local.entity.CartItemEntity
import com.yashas.shoplite.domain.model.CartItem
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val dao: CartDao
) : CartRepository {

    override fun getCart(): Flow<List<CartItem>> {
        return dao.getCartItems().map { entities ->
            entities.map { 
                CartItem(
                    productId = it.productId, 
                    name = it.name, 
                    price = it.price, 
                    imageUrl = it.imageUrl, 
                    quantity = it.quantity
                ) 
            }
        }
    }

    override suspend fun addToCart(product: Product, quantity: Int): Result<Unit> {
        return try {
            val existingItem = dao.getCartItemById(product.id)
            if (existingItem != null) {
                dao.updateQuantity(product.id, existingItem.quantity + quantity)
            } else {
                val discountedPrice = product.price - (product.price * (product.discountPercentage / 100))
                dao.insertOrUpdate(
                    CartItemEntity(
                        productId = product.id, 
                        name = product.name, 
                        price = discountedPrice, 
                        imageUrl = product.imageUrl, 
                        quantity = quantity
                    )
                )
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
