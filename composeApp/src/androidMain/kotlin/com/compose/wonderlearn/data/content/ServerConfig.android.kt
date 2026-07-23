package com.compose.wonderlearn.data.content

import com.compose.wonderlearn.BuildConfig

actual val isDebugBuild: Boolean = BuildConfig.DEBUG

/** 10.0.2.2 is the host machine as seen from the Android emulator. */
actual val devServerBaseUrl: String = "http://10.0.2.2:8080"
