package com.yashas.shoplite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yashas.shoplite.data.local.entity.CartItemEntity
import com.yashas.shoplite.data.local.entity.ProductEntity
import com.yashas.shoplite.data.local.entity.WishlistEntity
import com.yashas.shoplite.data.local.dao.CartDao
import com.yashas.shoplite.data.local.dao.ProductDao
import com.yashas.shoplite.data.local.dao.WishlistDao

@Database(entities = [CartItemEntity::class, ProductEntity::class, WishlistEntity::class], version = 4, exportSchema = false)
abstract class ShopLiteDatabase : RoomDatabase() {
    abstract val cartDao: CartDao
    abstract val productDao: ProductDao
    abstract val wishlistDao: WishlistDao
}
