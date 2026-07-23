package com.compose.wonderlearn.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidAppLockController(private val activity: Activity?) : AppLockController {
  override val lockSupported: Boolean = activity != null

  override fun lock(): Boolean {
    val act = activity ?: return false
    return runCatching {
      act.startLockTask()
      val am = act.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
    }.getOrDefault(false)
  }

  override fun unlock() {
    runCatching { activity?.stopLockTask() }
  }
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
