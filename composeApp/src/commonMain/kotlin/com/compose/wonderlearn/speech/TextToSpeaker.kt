package com.compose.wonderlearn.speech

interface TextToSpeaker {
  fun speak(text: String, languageTag: String): Boolean
}
