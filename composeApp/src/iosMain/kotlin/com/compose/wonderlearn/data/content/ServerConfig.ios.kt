package com.compose.wonderlearn.data.content

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
actual val isDebugBuild: Boolean = Platform.isDebugBinary

actual val devServerBaseUrl: String = "http://localhost:8080"
