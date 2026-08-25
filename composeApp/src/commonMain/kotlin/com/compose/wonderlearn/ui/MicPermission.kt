package com.compose.wonderlearn.ui

import androidx.compose.runtime.Composable

enum class MicPermission { GRANTED, DENIED, UNKNOWN }

interface MicPermissionState {
  val status: MicPermission
  fun request()
}

@Composable
expect fun rememberMicPermission(): MicPermissionState
