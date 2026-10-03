package com.yashas.shoplite.domain.usecase.settings

import com.yashas.shoplite.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ThemeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    fun getTheme(): Flow<String> = repository.getTheme()
    
    suspend fun setTheme(theme: String) = repository.setTheme(theme)
}
