package com.sashio.ops.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Brand Colors ---
val SashioGold = Color(0xFFD4AF37)
val SashioGoldLight = Color(0xFFF3E5AB)
val SashioGoldDark = Color(0xFFA67C00)

val SashioSlate = Color(0xFF0F172A)
val SashioSlateDark = Color(0xFF020617)
val SashioSlateCard = Color(0xFF1E293B)

val SashioTeal = Color(0xFF0D9488)
val SashioTealDark = Color(0xFF115E59)
val SashioTealLight = Color(0xFF5EEAD4)

val SashioGreen = Color(0xFF16A34A)
val SashioRed = Color(0xFFDC2626)
val SashioBlue = Color(0xFF2563EB)
val SashioLight = Color(0xFFF8FAFC)
val SashioTextMuted = Color(0xFF64748B)

private val DarkColorScheme = darkColorScheme(
    primary = SashioGold,
    onPrimary = SashioSlate,
    secondary = SashioTeal,
    background = SashioSlate,
    surface = SashioSlateCard,
    onBackground = Color.White,
    onSurface = Color.White,
    error = SashioRed
)

private val LightColorScheme = lightColorScheme(
    primary = SashioGold,
    onPrimary = Color.White,
    secondary = SashioTeal,
    background = SashioLight,
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    error = SashioRed
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

// Alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = SashioTheme(darkTheme = darkTheme, content = content)
