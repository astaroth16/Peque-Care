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
    onPrimaryContainer = Color(0xFF4F1822),
    secondary = EmeraldSuccess,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = Color(0xFF163D27),
    tertiary = IndigoPrimary,
    onTertiary = Color.White,
    tertiaryContainer = IndigoContainer,
    onTertiaryContainer = Color(0xFF183652),
    background = SlateLight,
    onBackground = SlateDark,
    surface = SurfaceCard,
    onSurface = SlateDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = SlateMedium,
    outline = Color(0xFFDECED1),
    outlineVariant = Color(0xFFEDE2E4)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkRosePrimary,
    onPrimary = Color(0xFF46161F),
    primaryContainer = Color(0xFF632833),
    onPrimaryContainer = RoseContainer,
    secondary = DarkMint,
    onSecondary = Color(0xFF143822),
    secondaryContainer = Color(0xFF265237),
    onSecondaryContainer = EmeraldContainer,
    tertiary = DarkSky,
    onTertiary = Color(0xFF153452),
    tertiaryContainer = Color(0xFF244B70),
    onTertiaryContainer = IndigoContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF524347),
    outlineVariant = Color(0xFF392F32)
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
