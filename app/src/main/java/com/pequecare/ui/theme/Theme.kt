package com.pequecare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = Color.White,
    primaryContainer = RoseContainer,
    onPrimaryContainer = Color(0xFF5A1C25),
    secondary = EmeraldSuccess,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = Color(0xFF1E4327),
    tertiary = IndigoPrimary,
    onTertiary = Color.White,
    tertiaryContainer = IndigoContainer,
    onTertiaryContainer = Color(0xFF1B3D5A),
    background = SlateLight,
    onBackground = SlateDark,
    surface = SurfaceCard,
    onSurface = SlateDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = SlateMedium,
    outline = Color(0xFFE2D6D8),
    outlineVariant = Color(0xFFEFE6E7)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkRosePrimary,
    onPrimary = Color(0xFF4C1D24),
    primaryContainer = Color(0xFF6B2F3A),
    onPrimaryContainer = RoseContainer,
    secondary = DarkMint,
    onSecondary = Color(0xFF1A3E26),
    secondaryContainer = Color(0xFF2E5B3D),
    onSecondaryContainer = EmeraldContainer,
    tertiary = DarkSky,
    onTertiary = Color(0xFF173858),
    tertiaryContainer = Color(0xFF275078),
    onTertiaryContainer = IndigoContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF544649),
    outlineVariant = Color(0xFF3B3134)
)

@Composable
fun PequeCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
