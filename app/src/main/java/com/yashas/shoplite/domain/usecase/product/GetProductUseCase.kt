package com.yashas.shoplite.domain.usecase.product

import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String): Result<Product> {
        if (productId.isBlank()) return Result.failure(IllegalArgumentException("Product ID cannot be empty"))
        return repository.getProduct(productId)
    }
}
