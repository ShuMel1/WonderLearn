@file:OptIn(ExperimentalForeignApi::class)

package com.compose.wonderlearn.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create

actual class AudioPlayer {

  private var player: AVAudioPlayer? = null

  actual fun play(bytes: ByteArray) {
    if (bytes.isEmpty()) return
    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
    AVAudioSession.sharedInstance().setActive(true, null)
    val data = bytes.usePinned { pinned ->
      NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    player = AVAudioPlayer(data = data, error = null).also { it.play() }
  }
}
