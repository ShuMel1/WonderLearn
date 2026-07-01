package com.compose.trackit.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

// iOS has no system-provided dynamic color scheme; fall back to the app palette.
@Composable
actual fun dynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme? = null
