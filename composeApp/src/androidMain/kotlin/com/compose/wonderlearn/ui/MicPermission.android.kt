package com.compose.wonderlearn.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberMicPermission(): MicPermissionState {
  val context = LocalContext.current
  fun granted() =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
      PackageManager.PERMISSION_GRANTED

  val statusState = remember {
    mutableStateOf(if (granted()) MicPermission.GRANTED else MicPermission.UNKNOWN)
  }

  val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { isGranted ->
    statusState.value = if (isGranted) MicPermission.GRANTED else MicPermission.DENIED
  }

  return remember {
    object : MicPermissionState {
      override val status: MicPermission get() = statusState.value
      override fun request() {
        if (granted()) statusState.value = MicPermission.GRANTED
        else launcher.launch(Manifest.permission.RECORD_AUDIO)
      }
    }
  }
}
