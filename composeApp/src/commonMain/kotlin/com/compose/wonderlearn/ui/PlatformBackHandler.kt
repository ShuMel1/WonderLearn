package com.compose.wonderlearn.ui

import androidx.compose.runtime.Composable

/** Handles the system back gesture on platforms that have one (Android). */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
