package com.yashas.shoplite.domain.usecase.cart

data class CartUseCases(
    val getCart: GetCartUseCase,
    val addToCart: AddToCartUseCase,
    val removeFromCart: RemoveFromCartUseCase,
    val updateCartQuantity: UpdateCartQuantityUseCase,
    val clearCart: ClearCartUseCase
)
