package com.compose.wonderlearn.audio

import android.media.MediaDataSource
import android.media.MediaPlayer

actual class AudioPlayer {

  private var player: MediaPlayer? = null

  actual fun play(bytes: ByteArray) {
    player?.release()
    player = MediaPlayer().apply {
      setDataSource(ByteArrayMediaDataSource(bytes))
      setOnCompletionListener { it.release() }
      setOnPreparedListener { it.start() }
      prepareAsync()
    }
  }
}

private class ByteArrayMediaDataSource(private val data: ByteArray) : MediaDataSource() {
  override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
    if (position >= data.size) return -1
    val count = minOf(size, data.size - position.toInt())
    System.arraycopy(data, position.toInt(), buffer, offset, count)
    return count
  }

  override fun getSize(): Long = data.size.toLong()

  override fun close() {}
}
