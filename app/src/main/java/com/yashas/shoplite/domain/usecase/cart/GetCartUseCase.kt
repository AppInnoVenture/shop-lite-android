package com.yashas.shoplite.domain.usecase.cart

import com.yashas.shoplite.domain.model.CartItem
import com.yashas.shoplite.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(): Flow<List<CartItem>> {
        return repository.getCart()
    }
}
