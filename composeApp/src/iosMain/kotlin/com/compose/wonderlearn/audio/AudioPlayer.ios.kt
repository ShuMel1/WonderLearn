@file:OptIn(ExperimentalForeignApi::class)

package com.compose.wonderlearn.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
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
  private var finishCurrent: (() -> Unit)? = null

  private val playbackDelegate = PlaybackDelegate(::onPlaybackEnded)

  actual suspend fun play(bytes: ByteArray) {
    if (bytes.isEmpty()) return
    stopCurrent()

    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
    AVAudioSession.sharedInstance().setActive(true, null)
    val data = bytes.usePinned { pinned ->
      NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }

    val next = AVAudioPlayer(data = data, error = null)
    next.setDelegate(playbackDelegate)
    player = next

    val guardMillis = ((next.duration + 1.0) * 1000).toLong().coerceIn(1_000, 30_000)

    withTimeoutOrNull(guardMillis) {
      suspendCancellableCoroutine { continuation ->
        var finished = false
        val finish = {
          if (!finished) {
            finished = true
            if (continuation.isActive) continuation.resume(Unit)
          }
        }
        finishCurrent = finish

        continuation.invokeOnCancellation { if (player === next) stopCurrent() }

        if (!next.play()) {
          player = null
          finishCurrent = null
          finish()
        }
      }
    }

    if (player === next) stopCurrent()
  }

  private fun onPlaybackEnded(ended: AVAudioPlayer) {
    ended.setDelegate(null)
    player = null
    val finish = finishCurrent
    finishCurrent = null
    finish?.invoke()
  }

  private fun stopCurrent() {
    player?.let { current ->
      current.setDelegate(null)
      current.stop()
    }
    player = null
    val finish = finishCurrent
    finishCurrent = null
    finish?.invoke()
  }
}

private class PlaybackDelegate(
  private val onEnded: (AVAudioPlayer) -> Unit,
) : NSObject(), AVAudioPlayerDelegateProtocol {

  override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) =
    onEnded(player)

  override fun audioPlayerDecodeErrorDidOccur(player: AVAudioPlayer, error: NSError?) =
    onEnded(player)
}
