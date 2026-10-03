package com.yashas.shoplite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val category: String,
    val name: String,
    val rating: Double,
    val price: Double,
    val discountPercentage: Double,
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
