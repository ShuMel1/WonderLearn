package com.compose.wonderlearn.data.analytics

/** Writes a debug line to the platform log (logcat on Android, console on iOS). */
expect fun platformLog(tag: String, message: String)
