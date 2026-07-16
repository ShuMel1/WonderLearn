package com.compose.wonderlearn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
  primary = BrandPrimary,
  onPrimary = BrandOnPrimary,
  secondary = Coral,
  tertiary = Teal,
  background = CreamBackground,
  onBackground = InkText,
  surface = CardSurface,
  onSurface = InkText,
  surfaceVariant = CreamBackground,
  onSurfaceVariant = MutedInk,
)

private val DarkColorScheme = darkColorScheme(
  primary = Grape,
  onPrimary = BrandOnPrimary,
  secondary = Coral,
  tertiary = Teal,
  background = DarkBackground,
  onBackground = DarkInk,
  surface = DarkSurface,
  onSurface = DarkInk,
  surfaceVariant = DarkSurface,
  onSurfaceVariant = MutedInk,
)

@Composable
fun WonderLearnTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    typography = Typography,
    content = content,
  )
}
