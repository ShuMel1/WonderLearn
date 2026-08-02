package com.compose.wonderlearn.data.analytics

import platform.Foundation.NSBundle

actual val platformName: String = "ios"

actual val appVersionName: String =
  NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "unknown"
