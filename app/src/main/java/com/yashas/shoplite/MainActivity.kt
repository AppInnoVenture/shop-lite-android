package com.yashas.shoplite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yashas.shoplite.domain.repository.SettingsRepository
import com.yashas.shoplite.navigation.nav_graph.AppNavHost
import com.yashas.shoplite.ui.theme.ShopLiteTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by settingsRepository.getTheme().collectAsState(initial = "System")
            
            val isDarkTheme = when (themePreference) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }
            
            ShopLiteTheme(darkTheme = isDarkTheme) {
                AppNavHost()
            }
        }
    }
}
