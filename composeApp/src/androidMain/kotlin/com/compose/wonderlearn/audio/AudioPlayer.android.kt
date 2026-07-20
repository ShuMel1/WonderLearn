package com.compose.wonderlearn.audio

import android.media.MediaDataSource
import android.media.MediaPlayer

actual class AudioPlayer {

  private var player: MediaPlayer? = null

  actual fun play(bytes: ByteArray) {
    if (bytes.isEmpty()) return
    releaseCurrent()

    val next = MediaPlayer()
    player = next
    next.setOnPreparedListener { prepared ->
      if (prepared === player) prepared.start() else prepared.release()
    }
    next.setOnCompletionListener { finished ->
      if (finished === player) player = null
      finished.release()
    }
    next.setOnErrorListener { failed, _, _ ->
      if (failed === player) player = null
      failed.release()
      true
    }

    try {
      next.setDataSource(ByteArrayMediaDataSource(bytes))
      next.prepareAsync()
    } catch (e: Exception) {
      if (player === next) player = null
      next.release()
    }
  }

  private fun releaseCurrent() {
    player?.let { current ->
      current.setOnPreparedListener(null)
      current.setOnCompletionListener(null)
      current.setOnErrorListener(null)
      runCatching { current.reset() }
      current.release()
    }
    player = null
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
