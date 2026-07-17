package com.compose.wonderlearn.audio

/** Plays a short audio clip provided as raw bytes. */
expect class AudioPlayer() {
  fun play(bytes: ByteArray)
}
