package com.compose.wonderlearn.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class AndroidTextToSpeaker(context: Context) : TextToSpeaker {

  private var ready = false
  private val tts = TextToSpeech(context.applicationContext) { status ->
    ready = status == TextToSpeech.SUCCESS
  }

  override fun speak(text: String, languageTag: String): Boolean {
    if (!ready) return false
    val locale = Locale.forLanguageTag(languageTag)
    if (tts.isLanguageAvailable(locale) < TextToSpeech.LANG_AVAILABLE) return false
    tts.language = locale
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    return true
  }
}
