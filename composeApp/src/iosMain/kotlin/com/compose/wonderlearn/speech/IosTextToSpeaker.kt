@file:OptIn(ExperimentalForeignApi::class)

package com.compose.wonderlearn.speech

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.setActive

class IosTextToSpeaker : TextToSpeaker {

  private val synthesizer = AVSpeechSynthesizer()

  override fun speak(text: String, languageTag: String): Boolean {
    val voice = AVSpeechSynthesisVoice.voiceWithLanguage(languageTag) ?: return false

    // iOS deactivates the audio session when the app is backgrounded; without reactivating it here
    // speech is silent after returning to the app until something else wakes the session.
    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
    AVAudioSession.sharedInstance().setActive(true, null)

    val utterance = AVSpeechUtterance(string = text)
    utterance.voice = voice
    synthesizer.speakUtterance(utterance)
    return true
  }
}
