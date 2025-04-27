package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.viewmodels.ThemeViewModel

private val LightColors = lightColorScheme(
    primary = Color(0xFF663399),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005E),
    secondary = Color(0xFF2196F3),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF001D36),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    error = Color(0xFFB00020)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9747FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A148C),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1565C0),
    onSecondaryContainer = Color(0xFFD6E4FF),
    surface = Color(0xFF121212),
    onSurface = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White,
    error = Color(0xFFCF6679)
)

@Composable
fun AppTheme(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    
    
    LaunchedEffect(isDarkMode) {
        Log.d("AppTheme", "Mode sombre actuel: $isDarkMode")
    }
    
    val colorScheme = remember(isDarkMode) {
        if (isDarkMode) DarkColors else LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 