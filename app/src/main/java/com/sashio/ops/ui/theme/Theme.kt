package com.sashio.ops.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SashioGold,
    onPrimary = SashioSlate,
    primaryContainer = SashioSlateCard,
    onPrimaryContainer = SashioGoldLight,
    secondary = SashioTeal,
    onSecondary = Color.White,
    secondaryContainer = SashioTealDark,
    onSecondaryContainer = SashioTealLight,
    tertiary = SashioGreen,
    background = SashioSlate,
    surface = SashioSlateCard,
    onBackground = Color.White,
    onSurface = Color.White,
    error = SashioRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SashioGold,
    onPrimary = Color.White,
    primaryContainer = SashioGoldLight,
    onPrimaryContainer = SashioSlate,
    secondary = SashioTeal,
    onSecondary = Color.White,
    secondaryContainer = SashioTealLight,
    onSecondaryContainer = SashioTealDark,
    tertiary = SashioGreen,
    background = SashioLight,
    surface = Color.White,
    onBackground = SachioTextDark,
    onSurface = SachioTextDark,
    error = SashioRed,
    onError = Color.White
)

@Composable
fun SashioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SashioSlate.toArgb()
            window.navigationBarColor = SashioSlate.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Uses your custom Typography from Type.kt
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = SashioTheme(darkTheme = darkTheme, content = content)
