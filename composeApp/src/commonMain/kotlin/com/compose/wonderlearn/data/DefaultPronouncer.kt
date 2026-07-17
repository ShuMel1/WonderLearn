package com.compose.wonderlearn.data

import com.compose.wonderlearn.audio.AudioPlayer
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.speech.TextToSpeaker

/**
 * Armenian is spoken from a bundled recording (no device TTS voice exists for it);
 * every other language uses the platform text-to-speech engine.
 */
class DefaultPronouncer(
  private val tts: TextToSpeaker,
  private val audioPlayer: AudioPlayer,
) : Pronouncer {

  override suspend fun pronounce(item: VocabularyItem, language: Language): Boolean {
    if (language == Language.ARMENIAN) {
      val clip = loadArmenianClip(item.id) ?: return false
      audioPlayer.play(clip)
      return true
    }
    return tts.speak(item.text(language), language.bcp47)
  }

  private suspend fun loadArmenianClip(wordId: String): ByteArray? =
    try {
      Res.readBytes("files/audio/hy_$wordId.m4a")
    } catch (e: Exception) {
      null
    }
}
