package com.compose.wonderlearn.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
  // iOS has no system back button at the root; in-stack back is handled by the navigator.
}
