package com.yashas.shoplite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yashas.shoplite.data.local.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist")
    fun getWishlistItems(): Flow<List<WishlistEntity>>
    
    @Query("SELECT * FROM wishlist WHERE productId = :id LIMIT 1")
    suspend fun getWishlistItemById(id: String): WishlistEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistEntity)
    
    @Delete
    suspend fun removeWishlistItem(item: WishlistEntity)
}
