
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
    onSecondary = Color.Black,
    secondaryContainer = SashioTealDark,
    onSecondaryContainer = SashioTealLight,
    tertiary = SashioGreen,
    background = SashioSlate,
    surface = SashioSlateCard,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF303030),
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF777777),
    error = SashioRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SashioGoldDark,
    onPrimary = Color.White,
    primaryContainer = SashioGoldLight,
    onPrimaryContainer = SashioSlate,
    secondary = SashioTealDark,
    onSecondary = Color.White,
    secondaryContainer = SashioTealLight,
    onSecondaryContainer = SashioTealDark,
    tertiary = SashioGreen,
    background = SashioLight,
    surface = Color.White,
    onBackground = SachioTextDark,
    onSurface = SachioTextDark,
    surfaceVariant = Color(0xFFECECEC),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFF777777),
    error = SashioRed,
    onError = Color.White
)

@Composable
fun SashioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val systemBarColor = colorScheme.background.toArgb()

            window.statusBarColor = systemBarColor
            window.navigationBarColor = systemBarColor

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    SashioTheme(
        darkTheme = darkTheme,
        content = content
    )
}
