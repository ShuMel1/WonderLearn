package com.compose.wonderlearn.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** iOS cannot lock the app itself; Guided Access is enabled by the parent from Settings. */
private class IosAppLockController : AppLockController {
  override val lockSupported: Boolean = false
  override fun lock(): Boolean = false
  override fun unlock() = Unit
}

@Composable
actual fun rememberAppLockController(): AppLockController = remember { IosAppLockController() }
