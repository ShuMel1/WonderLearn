package com.compose.wonderlearn.speech

sealed interface SpeechOutcome {
  data class Heard(val transcripts: List<String>) : SpeechOutcome

  data object NoSpeech : SpeechOutcome

  data object Unavailable : SpeechOutcome

  data object Error : SpeechOutcome
}

interface SpeechRecognizer {

  suspend fun isAvailable(languageTag: String): Boolean

  suspend fun listen(languageTag: String): SpeechOutcome

  fun cancel()
}
