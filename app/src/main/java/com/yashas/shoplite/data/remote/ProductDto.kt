package com.yashas.shoplite.data.remote

data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val category: String,
    val thumbnail: String,
    val images: List<String>?,
    val brand: String?,
    val sku: String?,
    val warrantyInformation: String?,
    val shippingInformation: String?,
    val returnPolicy: String?,
    val dimensions: DimensionsDto?,
    val reviews: List<ReviewDto>?
)
