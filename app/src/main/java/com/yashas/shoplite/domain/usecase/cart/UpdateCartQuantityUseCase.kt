package com.yashas.shoplite.domain.usecase.cart

import com.yashas.shoplite.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(productId: String, quantity: Int): Result<Unit> {
        if (productId.isBlank()) return Result.failure(IllegalArgumentException("Invalid User or Product ID"))
        return repository.updateQuantity(productId, quantity)
    }
}
