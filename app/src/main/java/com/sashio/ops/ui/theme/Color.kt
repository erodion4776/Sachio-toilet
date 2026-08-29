package com.sashio.ops.ui.theme

import androidx.compose.ui.graphics.Color

// --- Sachio Corporate Palette Color Theme (Material 3) ---
val SachioBlack = Color(0xFF000000)
val SachioCharcoal = Color(0xFF1A1A1A)
val SachioMustard = Color(0xFFD2CC46) // Extracted from right shape of logo
val SachioGrey = Color(0xFF808080)    // Extracted from left shape of logo
val SachioLightBackground = Color(0xFFF7F7F7)
val SachioTextDark = Color(0xFF1E1E1E)

// Theme variables used in the app
val VibrantBackground = SachioLightBackground
val VibrantOnSurface = SachioBlack
val VibrantSurfaceVariant = Color(0xFFE5E5E5)
val VibrantBorder = SachioGrey

// Map existing variables so the entire Sashio app transforms instantly & perfectly
val SashioTeal = SachioMustard        // Use mustard as the primary brand color
val SashioTealDark = SachioCharcoal
val SashioTealLight = Color(0xFFE5E072)

val SashioGold = SachioMustard        // Map gold to mustard
val SashioGoldDark = Color(0xFFA6A02C) // Darker mustard for badge/text contrast
val SashioGoldLight = Color(0xFFF1EEA8)

val SashioSlate = SachioBlack         // The primary dark tone
val SashioSlateDark = Color(0xFF0D0D0D)
val SashioSlateCard = SachioCharcoal
val SashioCardDark = SachioCharcoal

val SashioLight = SachioLightBackground
val SashioTextMuted = SachioGrey

// Functional status colors
val SashioGreen = Color(0xFF10B981)   // Status green
val SashioGreenLight = Color(0xFF86EFAC)

val SashioRed = Color(0xFFEF4444)     // Status red
val SashioRedLight = Color(0xFFFCA5A5)

val SashioBlue = Color(0xFF3B82F6)    // Status blue
val SashioBlueLight = Color(0xFF93C5FD)

// Base Material theme mappings
val Purple80 = SachioMustard
val PurpleGrey80 = SachioGrey
val Pink80 = SachioMustard

val Purple40 = SachioMustard
val PurpleGrey40 = SachioGrey
val Pink40 = SachioMustard
