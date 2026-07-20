@file:OptIn(ExperimentalForeignApi::class)

package com.compose.wonderlearn.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class AudioPlayer {

  private var player: AVAudioPlayer? = null
  private var delegate: PlaybackDelegate? = null
  private var finishCurrent: (() -> Unit)? = null

  actual suspend fun play(bytes: ByteArray) {
    if (bytes.isEmpty()) return
    stopCurrent()

    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
    AVAudioSession.sharedInstance().setActive(true, null)
    val data = bytes.usePinned { pinned ->
      NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }

    suspendCancellableCoroutine { continuation ->
      var finished = false
      val finish = {
        if (!finished) {
          finished = true
          if (continuation.isActive) continuation.resume(Unit)
        }
      }
      finishCurrent = finish

      val next = AVAudioPlayer(data = data, error = null)
      val playbackDelegate = PlaybackDelegate {
        if (player === next) {
          player = null
          delegate = null
          finishCurrent = null
        }
        finish()
      }
      next.setDelegate(playbackDelegate)
      player = next
      delegate = playbackDelegate

      continuation.invokeOnCancellation { if (player === next) stopCurrent() }

      if (!next.play()) finish()
    }
  }

  private fun stopCurrent() {
    player?.let { current ->
      current.setDelegate(null)
      current.stop()
    }
    player = null
    delegate = null
    finishCurrent?.invoke()
    finishCurrent = null
  }
}

private class PlaybackDelegate(
  private val onDone: () -> Unit,
) : NSObject(), AVAudioPlayerDelegateProtocol {

  override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) = onDone()

  override fun audioPlayerDecodeErrorDidOccur(player: AVAudioPlayer, error: NSError?) = onDone()
}
