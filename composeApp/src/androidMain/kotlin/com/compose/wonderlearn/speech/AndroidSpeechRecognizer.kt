package com.compose.wonderlearn.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as PlatformRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class AndroidSpeechRecognizer(private val context: Context) : SpeechRecognizer {

  private var recognizer: PlatformRecognizer? = null

  override suspend fun isAvailable(languageTag: String): Boolean = withContext(Dispatchers.Main) {
    PlatformRecognizer.isRecognitionAvailable(context)
  }

  override suspend fun listen(languageTag: String): SpeechOutcome = withContext(Dispatchers.Main) {
    if (!PlatformRecognizer.isRecognitionAvailable(context)) return@withContext SpeechOutcome.Unavailable

    val onDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val outcome = attempt(languageTag, onDevice)
    if (outcome == SpeechOutcome.Unavailable && onDevice) {
      attempt(languageTag, onDevice = false)
    } else {
      outcome
    }
  }

  private suspend fun attempt(languageTag: String, onDevice: Boolean): SpeechOutcome {
    val speech = createRecognizer(onDevice) ?: return SpeechOutcome.Unavailable
    recognizer = speech

    return suspendCancellableCoroutine { continuation ->
      var settled = false
      fun finish(outcome: SpeechOutcome) {
        if (settled) return
        settled = true
        speech.destroy()
        if (recognizer === speech) recognizer = null
        if (continuation.isActive) continuation.resume(outcome)
      }

      speech.setRecognitionListener(object : RecognitionListener {
        override fun onResults(results: Bundle?) {
          val words = results?.getStringArrayList(PlatformRecognizer.RESULTS_RECOGNITION)
          finish(if (words.isNullOrEmpty()) SpeechOutcome.NoSpeech else SpeechOutcome.Heard(words))
        }

        override fun onError(error: Int) = finish(mapError(error))
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
      })

      continuation.invokeOnCancellation {
        settled = true
        speech.cancel()
        speech.destroy()
        if (recognizer === speech) recognizer = null
      }

      try {
        speech.startListening(listenIntent(languageTag, onDevice))
      } catch (e: Exception) {
        finish(SpeechOutcome.Error)
      }
    }
  }

  override fun cancel() {
    recognizer?.cancel()
    recognizer?.destroy()
    recognizer = null
  }

  private fun createRecognizer(onDevice: Boolean): PlatformRecognizer? = try {
    if (onDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PlatformRecognizer.createOnDeviceSpeechRecognizer(context)
    } else {
      PlatformRecognizer.createSpeechRecognizer(context)
    }
  } catch (e: Exception) {
    try {
      PlatformRecognizer.createSpeechRecognizer(context)
    } catch (e: Exception) {
      null
    }
  }

  private fun listenIntent(languageTag: String, onDevice: Boolean) =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
      putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
      putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 900L)
      putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 900L)
      if (onDevice) putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
    }

  private fun mapError(error: Int): SpeechOutcome = when (error) {
    PlatformRecognizer.ERROR_NO_MATCH,
    PlatformRecognizer.ERROR_SPEECH_TIMEOUT,
    -> SpeechOutcome.NoSpeech
    PlatformRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
    PlatformRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
    -> SpeechOutcome.Unavailable
    else -> SpeechOutcome.Error
  }
}
