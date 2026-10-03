package com.yashas.shoplite.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getTheme(): Flow<String>
    suspend fun setTheme(theme: String)
    
    fun getCurrency(): Flow<String>
    suspend fun setCurrency(currency: String)
}
