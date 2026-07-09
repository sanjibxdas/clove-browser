package com.clove.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** Provides the active Safari palette (light chrome, or dark when in Private mode). */
val LocalSafariPalette = staticCompositionLocalOf { LightSafari }

@Composable
fun CloveTheme(
    private: Boolean = false,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = when {
        private -> PrivateSafari
        darkTheme -> DarkSafari
        else -> LightSafari
    }
    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accent,
            background = palette.pageBackground,
            surface = palette.glassElevated,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            background = palette.pageBackground,
            surface = palette.glassElevated,
        )
    }

    CompositionLocalProvider(LocalSafariPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
