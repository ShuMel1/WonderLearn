package com.compose.trackit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = Purple80,
  secondary = PurpleGrey80,
  tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
  primary = Purple40,
  secondary = PurpleGrey40,
  tertiary = Pink40
)

/**
 * Returns a platform-provided dynamic color scheme, or null when the platform
 * has none. Android 12+ supplies a Material You scheme; iOS always returns null.
 */
@Composable
expect fun dynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme?

@Composable
fun TrackItTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+; ignored on platforms without it.
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit
) {
  val dynamic = if (dynamicColor) dynamicColorSchemeOrNull(darkTheme) else null
  val colorScheme = dynamic ?: if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
