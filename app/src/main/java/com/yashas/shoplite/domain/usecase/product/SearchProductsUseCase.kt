package com.yashas.shoplite.domain.usecase.product

import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.ProductRepository
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(query: String): Result<List<Product>> {
        if (query.isBlank()) return repository.getProducts()
        return repository.searchProducts(query)
    }
}
