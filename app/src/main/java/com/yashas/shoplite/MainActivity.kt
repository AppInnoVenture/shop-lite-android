package com.yashas.shoplite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }
        
        setContent {
            val themePreference by settingsRepository.getTheme().collectAsState(initial = null)
            
            if (themePreference != null) {
                keepSplash = false
                val isDarkTheme = when (themePreference) {
                    "Light" -> false
                    "Dark" -> true
                    else -> isSystemInDarkTheme()
                }
                
                LaunchedEffect(isDarkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = androidx.activity.SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        ) { isDarkTheme },
                        navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        ) { isDarkTheme }
                    )
                }
                
                ShopLiteTheme(darkTheme = isDarkTheme) {
                    AppNavHost()
                }
            }
        }
    }
}
