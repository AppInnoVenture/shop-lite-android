package com.yashas.shoplite.navigation.routes

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Main : Destination

    @Serializable
    data class ProductDetails(val id: String) : Destination

    @Serializable
    data object Cart : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data object Wishlist : Destination
}
