package com.yashas.shoplite.domain.usecase.cart

import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.CartRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(product: Product, quantity: Int = 1): Result<Unit> {
      //  if (userId.isBlank()) return Result.failure(IllegalArgumentException("User must be logged in"))
        return repository.addToCart(product, quantity)
    }
}
