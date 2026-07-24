package com.compose.wonderlearn.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidAppLockController(private val activity: Activity?) : AppLockController {
  override val lockSupported: Boolean = activity != null

  override fun lock(): Boolean {
    val act = activity ?: return false
    // startLockTask() updates the lock state asynchronously, so checking it straight after races.
    // Instead, decide from whether screen pinning is enabled in Settings — if it is off, pinning
    // silently does nothing, and the caller should guide the parent to turn it on.
    if (!screenPinningEnabled(act)) return false
    return runCatching { act.startLockTask(); true }.getOrDefault(false)
  }

  override fun unlock() {
    runCatching { activity?.stopLockTask() }
  }

  private fun screenPinningEnabled(act: Activity): Boolean =
    runCatching {
      Settings.Secure.getInt(act.contentResolver, "lock_to_app_enabled", 0) == 1
    }.getOrDefault(false)
}

private fun Context.findActivity(): Activity? {
  var ctx: Context? = this
  while (ctx is ContextWrapper) {
    if (ctx is Activity) return ctx
    ctx = ctx.baseContext
  }
  return null
}

@Composable
actual fun rememberAppLockController(): AppLockController {
  val activity = LocalContext.current.findActivity()
  return remember(activity) { AndroidAppLockController(activity) }
}
