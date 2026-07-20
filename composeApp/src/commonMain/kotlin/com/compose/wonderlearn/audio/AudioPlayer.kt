package com.compose.wonderlearn.audio

/** Plays a short audio clip provided as raw bytes, returning once playback finishes. */
expect class AudioPlayer() {
  suspend fun play(bytes: ByteArray)
}
