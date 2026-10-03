package com.yashas.shoplite.domain.usecase.cart

import com.yashas.shoplite.domain.repository.CartRepository
import javax.inject.Inject

class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID cannot be empty"))
        return repository.clearCart()
    }
}
