package com.yashas.shoplite.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import com.yashas.shoplite.domain.usecase.settings.ThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeUseCase: ThemeUseCase,
    private val currencyUseCase: CurrencyUseCase
) : ViewModel() {

    private val _theme = MutableStateFlow("System")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _currency = MutableStateFlow("USD")
    val currency: StateFlow<String> = _currency.asStateFlow()

    init {
        viewModelScope.launch {
            themeUseCase.getTheme().collect {
                _theme.value = it
            }
        }
        viewModelScope.launch {
            currencyUseCase.getCurrency().collect {
                _currency.value = it
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            themeUseCase.setTheme(theme)
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            currencyUseCase.setCurrency(currency)
        }
    }
}
