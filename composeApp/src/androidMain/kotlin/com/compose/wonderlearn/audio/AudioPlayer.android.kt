package com.compose.wonderlearn.audio

import android.media.MediaDataSource
import android.media.MediaPlayer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class AudioPlayer {

  private var player: MediaPlayer? = null
  private var finishCurrent: (() -> Unit)? = null

  actual suspend fun play(bytes: ByteArray) {
    if (bytes.isEmpty()) return
    releaseCurrent()

    suspendCancellableCoroutine { continuation ->
      val next = MediaPlayer()
      player = next

      var finished = false
      val finish = {
        if (!finished) {
          finished = true
          if (continuation.isActive) continuation.resume(Unit)
        }
      }
      finishCurrent = finish

      next.setOnPreparedListener { prepared ->
        if (prepared === player) prepared.start() else prepared.release()
      }
      next.setOnCompletionListener { done ->
        if (done === player) {
          player = null
          finishCurrent = null
        }
        done.release()
        finish()
      }
      next.setOnErrorListener { failed, _, _ ->
        if (failed === player) {
          player = null
          finishCurrent = null
        }
        failed.release()
        finish()
        true
      }

      continuation.invokeOnCancellation { if (player === next) releaseCurrent() }

      try {
        next.setDataSource(ByteArrayMediaDataSource(bytes))
        next.prepareAsync()
      } catch (e: Exception) {
        if (player === next) {
          player = null
          finishCurrent = null
        }
        next.release()
        finish()
      }
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
    finishCurrent?.invoke()
    finishCurrent = null
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
