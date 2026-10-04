package com.yashas.shoplite

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yashas.shoplite.domain.repository.SettingsRepository
import com.yashas.shoplite.navigation.nav_graph.ShopLiteNavGraph
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
            var cachedTheme by rememberSaveable { mutableStateOf<String?>(null) }
            val themePreference by settingsRepository.getTheme().collectAsState(initial = cachedTheme)
            
            if (themePreference != null) {
                cachedTheme = themePreference
                keepSplash = false
                val isDarkTheme = when (themePreference) {
                    "Light" -> false
                    "Dark" -> true
                    else -> isSystemInDarkTheme()
                }

                DisposableEffect(isDarkTheme) {
                    val systemBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(
                            Color.TRANSPARENT
                        )
                    } else {
                        SystemBarStyle.light(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT
                        )
                    }

                    enableEdgeToEdge(
                        statusBarStyle = systemBarStyle,
                        navigationBarStyle = systemBarStyle
                    )
                    onDispose {}
                }
                
                ShopLiteTheme(darkTheme = isDarkTheme) {
                    ShopLiteNavGraph()
                }
            }
        }
    }
}
