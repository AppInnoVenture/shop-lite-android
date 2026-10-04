package com.yashas.shoplite.navigation.routes

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
sealed interface Destination {
    @Keep
    @Serializable
    @SerialName("main_screen")
    data object Main : Destination

    @Keep
    @Serializable
    @SerialName("product_details_screen")
    data class ProductDetails(val id: String) : Destination

    @Keep
    @Serializable
    @SerialName("cart_screen")
    data object Cart : Destination

    @Keep
    @Serializable
    @SerialName("settings_screen")
    data object Settings : Destination

    @Keep
    @Serializable
    @SerialName("wishlist_screen")
    data object Wishlist : Destination
}
