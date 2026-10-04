package com.yashas.shoplite.domain.usecase.catalog

import com.yashas.shoplite.domain.usecase.product.GetProductsUseCase
import com.yashas.shoplite.domain.usecase.product.SearchProductsUseCase
import com.yashas.shoplite.domain.usecase.product.GetCategoriesUseCase
import com.yashas.shoplite.domain.usecase.product.GetProductsByCategoryUseCase

data class CatalogUseCases(
    val getProducts: GetProductsUseCase,
    val searchProducts: SearchProductsUseCase,
    val getCategories: GetCategoriesUseCase,
    val getProductsByCategory: GetProductsByCategoryUseCase
)
