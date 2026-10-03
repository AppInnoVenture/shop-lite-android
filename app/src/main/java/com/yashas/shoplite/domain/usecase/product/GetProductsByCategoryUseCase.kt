package com.yashas.shoplite.domain.usecase.product

import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsByCategoryUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(category: String): Result<List<Product>> {
        return repository.getProductsByCategory(category)
    }
}
