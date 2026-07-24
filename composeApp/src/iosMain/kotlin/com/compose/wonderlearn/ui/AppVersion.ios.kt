package com.compose.wonderlearn.ui

import platform.Foundation.NSBundle

actual fun appVersionName(): String =
  NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
