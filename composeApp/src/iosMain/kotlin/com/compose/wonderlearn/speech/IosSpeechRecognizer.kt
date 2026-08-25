@file:OptIn(ExperimentalForeignApi::class)

package com.compose.wonderlearn.speech

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Foundation.NSLocale
import platform.Foundation.NSLock
import platform.Foundation.NSLog
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionResult
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFTranscription
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import kotlin.coroutines.resume

private const val LISTEN_WINDOW_NANOS = 6_000_000_000L
private const val SILENCE_NANOS = 900_000_000L
private const val NO_SPEECH_ERROR = 1110L

class IosSpeechRecognizer : SpeechRecognizer {

  private var task: SFSpeechRecognitionTask? = null
  private var onDeviceUsable = true

  override suspend fun isAvailable(languageTag: String): Boolean {
    val recognizer = SFSpeechRecognizer(locale = NSLocale(localeIdentifier = languageTag))
      ?: return false
    return recognizer.available
  }

  override suspend fun listen(languageTag: String): SpeechOutcome {
    val outcome = attempt(languageTag, onDevice = onDeviceUsable)
    if (outcome != SpeechOutcome.Error || !onDeviceUsable) return outcome
    onDeviceUsable = false
    return attempt(languageTag, onDevice = false)
  }

  private suspend fun attempt(languageTag: String, onDevice: Boolean): SpeechOutcome {
    val recognizer = SFSpeechRecognizer(locale = NSLocale(localeIdentifier = languageTag))
      ?: return SpeechOutcome.Unavailable
    if (!recognizer.available) return SpeechOutcome.Unavailable
    if (onDevice && !recognizer.supportsOnDeviceRecognition) return SpeechOutcome.Error

    val session = AVAudioSession.sharedInstance()
    session.setCategory(
      AVAudioSessionCategoryPlayAndRecord,
      withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker,
      error = null,
    )
    session.setActive(true, error = null)

    val request = SFSpeechAudioBufferRecognitionRequest().apply {
      requiresOnDeviceRecognition = onDevice
      shouldReportPartialResults = true
    }
    val engine = AVAudioEngine()
    val input = engine.inputNode
    val format = input.outputFormatForBus(0uL)
    if (format.sampleRate <= 0.0 || format.channelCount == 0u) {
      NSLog("Wisekins: input format not ready (rate=%f channels=%u)", format.sampleRate, format.channelCount)
      return SpeechOutcome.Error
    }

    return suspendCancellableCoroutine { continuation ->
      val lock = NSLock()
      var settled = false
      var heardGeneration = 0

      fun stopAudio() {
        engine.stop()
        input.removeTapOnBus(0uL)
      }

      fun endAfterSilence() {
        lock.lock()
        heardGeneration += 1
        val generation = heardGeneration
        lock.unlock()
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, SILENCE_NANOS), dispatch_get_main_queue()) {
          lock.lock()
          val stale = settled || heardGeneration != generation
          lock.unlock()
          if (!stale) {
            stopAudio()
            request.endAudio()
          }
        }
      }

      fun finish(outcome: SpeechOutcome) {
        lock.lock()
        if (settled) {
          lock.unlock()
          return
        }
        settled = true
        lock.unlock()
        stopAudio()
        task?.cancel()
        task = null
        if (continuation.isActive) continuation.resume(outcome)
      }

      input.installTapOnBus(0uL, bufferSize = 1024u, format = format) { buffer, _ ->
        if (buffer != null) request.appendAudioPCMBuffer(buffer)
      }

      task = recognizer.recognitionTaskWithRequest(request) { result, error ->
        val transcripts = result?.transcripts()
        when {
          !transcripts.isNullOrEmpty() && (result?.final == true) ->
            finish(SpeechOutcome.Heard(transcripts))
          error != null -> {
            if (!error.heardNothing()) {
              NSLog("Wisekins: recognition failed: %@", error.localizedDescription)
            }
            finish(
              when {
                !transcripts.isNullOrEmpty() -> SpeechOutcome.Heard(transcripts)
                error.heardNothing() -> SpeechOutcome.NoSpeech
                else -> SpeechOutcome.Error
              },
            )
          }
          result?.final == true -> finish(SpeechOutcome.NoSpeech)
          !transcripts.isNullOrEmpty() -> endAfterSilence()
        }
      }

      engine.prepare()
      val started = memScoped {
        val errorRef = alloc<ObjCObjectVar<NSError?>>()
        val ok = engine.startAndReturnError(errorRef.ptr)
        if (!ok) NSLog("Wisekins: audio engine failed to start: %@", errorRef.value?.localizedDescription ?: "unknown")
        ok
      }
      if (!started) {
        finish(SpeechOutcome.Error)
        return@suspendCancellableCoroutine
      }

      continuation.invokeOnCancellation { finish(SpeechOutcome.Error) }

      dispatch_after(dispatch_time(DISPATCH_TIME_NOW, LISTEN_WINDOW_NANOS), dispatch_get_main_queue()) {
        lock.lock()
        val alreadyDone = settled
        lock.unlock()
        if (!alreadyDone) {
          stopAudio()
          request.endAudio()
        }
      }
    }
  }

  override fun cancel() {
    task?.cancel()
    task = null
  }
}

private fun NSError.heardNothing(): Boolean = code == NO_SPEECH_ERROR

@OptIn(ExperimentalForeignApi::class)
private fun SFSpeechRecognitionResult.transcripts(): List<String> {
  val best = bestTranscription.formattedString
  val all = transcriptions.mapNotNull { (it as? SFTranscription)?.formattedString }
  return (listOf(best) + all).filter { it.isNotBlank() }.distinct()
}
