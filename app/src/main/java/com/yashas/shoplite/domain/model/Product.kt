package com.yashas.shoplite.domain.model

data class Product(
    val id: String,
    val category: String,
    val name: String,
    val rating: Double,
    val price: Double,
    val discountPercentage: Double,
    val imageUrl: String,
    val images: List<String>,
    val description: String,
    val stock: Int,
    val brand: String,
    val sku: String,
    val warrantyInformation: String,
    val shippingInformation: String,
    val returnPolicy: String,
    val dimensions: Dimensions,
    val reviews: List<Review>
)

data class Dimensions(
    val width: Double,
    val height: Double,
    val depth: Double
)

data class Review(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String
)
