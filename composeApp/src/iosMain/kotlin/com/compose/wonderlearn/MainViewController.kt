package com.compose.wonderlearn

import androidx.compose.ui.window.ComposeUIViewController
import com.compose.wonderlearn.di.initKoin
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
  initKoin()
  return ComposeUIViewController { App() }
}
