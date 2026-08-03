package com.compose.wonderlearn.data.analytics

actual fun platformLog(tag: String, message: String) {
  println("$tag: $message")
}
