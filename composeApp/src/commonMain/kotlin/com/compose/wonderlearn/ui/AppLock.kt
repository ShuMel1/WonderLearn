package com.compose.wonderlearn.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * Parent-activated screen lock that keeps a child inside the app.
 *
 * Android pins the screen ([lockSupported] is true); the child cannot reach other apps, and on a
 * device with a lock PIN cannot even unpin. iOS has no programmatic equivalent, so [lockSupported]
 * is false there and the UI guides the parent to Guided Access instead.
 */
interface AppLockController {
  val lockSupported: Boolean

  /** Returns true only if the screen is actually locked now (Android pinning may be off in Settings). */
  fun lock(): Boolean
  fun unlock()
}

@Composable
expect fun rememberAppLockController(): AppLockController

/** App-wide lock state and actions, so any screen can start the lock and offer the unlock. */
data class LockUi(
  val supported: Boolean,
  val locked: Boolean,
  /** Returns true if the lock actually engaged; false means the parent must enable pinning first. */
  val requestLock: () -> Boolean,
  val requestUnlock: () -> Unit,
)

val LocalLockUi = compositionLocalOf { LockUi(supported = false, locked = false, { false }, {}) }
