package com.compose.wonderlearn.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
import platform.Speech.SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusDenied
import platform.Speech.SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusRestricted

@Composable
actual fun rememberMicPermission(): MicPermissionState {
  val statusState = remember { mutableStateOf(currentStatus()) }

  return remember {
    object : MicPermissionState {
      override val status: MicPermission get() = statusState.value

      override fun request() {
        SFSpeechRecognizer.requestAuthorization {
          AVAudioSession.sharedInstance().requestRecordPermission { _ ->
            statusState.value = currentStatus()
          }
        }
      }
    }
  }
}

private fun currentStatus(): MicPermission {
  val speechStatus = SFSpeechRecognizer.authorizationStatus()
  val micPermission = AVAudioSession.sharedInstance().recordPermission
  val speechAuthorized = speechStatus == SFSpeechRecognizerAuthorizationStatusAuthorized
  val micGranted = micPermission == AVAudioSessionRecordPermissionGranted
  val denied = speechStatus == SFSpeechRecognizerAuthorizationStatusDenied ||
    speechStatus == SFSpeechRecognizerAuthorizationStatusRestricted ||
    micPermission == AVAudioSessionRecordPermissionDenied
  return when {
    speechAuthorized && micGranted -> MicPermission.GRANTED
    denied -> MicPermission.DENIED
    else -> MicPermission.UNKNOWN
  }
}
