package com.compose.wonderlearn.speech

import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance

class IosTextToSpeaker : TextToSpeaker {

  private val synthesizer = AVSpeechSynthesizer()

  override fun speak(text: String, languageTag: String): Boolean {
    val voice = AVSpeechSynthesisVoice.voiceWithLanguage(languageTag) ?: return false
    val utterance = AVSpeechUtterance(string = text)
    utterance.voice = voice
    synthesizer.speakUtterance(utterance)
    return true
  }
}
