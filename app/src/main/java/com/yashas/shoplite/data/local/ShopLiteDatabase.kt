package com.yashas.shoplite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yashas.shoplite.data.local.entity.CartItemEntity
import com.yashas.shoplite.data.local.entity.ProductEntity
import com.yashas.shoplite.data.local.entity.FavoriteEntity
import com.yashas.shoplite.data.local.dao.CartDao
import com.yashas.shoplite.data.local.dao.ProductDao
import com.yashas.shoplite.data.local.dao.FavoriteDao

@Database(entities = [CartItemEntity::class, ProductEntity::class, FavoriteEntity::class], version = 3, exportSchema = false)
abstract class ShopLiteDatabase : RoomDatabase() {
    abstract val cartDao: CartDao
    abstract val productDao: ProductDao
    abstract val favoriteDao: FavoriteDao
}
