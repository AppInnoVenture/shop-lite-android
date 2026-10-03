package com.yashas.shoplite.domain.usecase.settings

import com.yashas.shoplite.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CurrencyUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    fun getCurrency(): Flow<String> = repository.getCurrency()
    
    suspend fun setCurrency(currency: String) = repository.setCurrency(currency)
    
    fun getRate(currency: String): Double {
        return when(currency) {
            "USD" -> 1.0
            "INR" -> 86.5
            "EUR" -> 0.92
            "GBP" -> 0.79
            else -> 1.0
        }
    }
    
    fun getSymbol(currency: String): String {
        return when(currency) {
            "USD" -> "$"
            "INR" -> "₹"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> "$"
        }
    }
    
    fun formatPrice(price: Double, currency: String): String {
        val converted = price * getRate(currency)
        val symbol = getSymbol(currency)
        return String.format("%s%.2f", symbol, converted)
    }
}
