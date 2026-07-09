package com.clove.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ---- iOS system-ish accents ----
val IOSBlue = Color(0xFF007AFF)
val IOSGray = Color(0xFF8E8E93)
val IOSRed = Color(0xFFFF3B30)
val IOSGreen = Color(0xFF34C759)
val IOSPurple = Color(0xFF9B6CFF)

/**
 * The colours that describe one "environment" of the browser chrome.
 * We swap the whole palette when entering Private Browsing (dark, purple-tinted),
 * exactly like Safari does.
 */
@Immutable
data class SafariPalette(
    val isDark: Boolean,
    /** Page area background shown before a site paints / on the start page. */
    val pageBackground: Color,
    /** Frosted glass fill for the floating bars & sheets. */
    val glass: Color,
    /** A slightly stronger glass for buttons / pills sitting on top of glass. */
    val glassElevated: Color,
    /** Hairline highlight along the top edge of glass surfaces. */
    val glassBorder: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val fieldFill: Color,
    val separator: Color,
    val scrim: Color,
)

val LightSafari = SafariPalette(
    isDark = false,
    pageBackground = Color(0xFFF2F2F7),
    glass = Color(0xE6FFFFFF),
    glassElevated = Color(0xFFFFFFFF),
    glassBorder = Color(0x59FFFFFF),
    accent = IOSBlue,
    textPrimary = Color(0xFF1C1C1E),
    textSecondary = Color(0xFF8E8E93),
    fieldFill = Color(0xFFEDEDF2),
    separator = Color(0x1F000000),
    scrim = Color(0x66000000),
)

val DarkSafari = SafariPalette(
    isDark = true,
    pageBackground = Color(0xFF1C1C1E),
    glass = Color(0xE621262B),
    glassElevated = Color(0xFF2C2C2E),
    glassBorder = Color(0x40FFFFFF),
    accent = IOSBlue,
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFF98989F),
    fieldFill = Color(0xFF2C2C2E),
    separator = Color(0x1FFFFFFF),
    scrim = Color(0x99000000),
)

val PrivateSafari = SafariPalette(
    isDark = true,
    pageBackground = Color(0xFF1C1C1E),
    glass = Color(0xE61C1C24),
    glassElevated = Color(0xFF2C2C34),
    glassBorder = Color(0x40FFFFFF),
    accent = IOSPurple,
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFF9A9AA2),
    fieldFill = Color(0xFF2C2C34),
    separator = Color(0x1FFFFFFF),
    scrim = Color(0x99000000),
)
