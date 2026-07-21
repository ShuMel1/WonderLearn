package com.compose.wonderlearn.data

import com.compose.wonderlearn.audio.AudioPlayer
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.speech.TextToSpeaker

/**
 * A language with bundled recordings is spoken from them; anything else falls back to the
 * platform text-to-speech engine, which not every language has a voice for.
 */
class DefaultPronouncer(
  private val tts: TextToSpeaker,
  private val audioPlayer: AudioPlayer,
) : Pronouncer {

  override suspend fun pronounce(item: VocabularyItem, language: Language): Boolean {
    if (language.hasRecordedAudio) {
      val clip = loadClip(language, item.id)
      if (clip != null) {
        audioPlayer.play(clip)
        return true
      }
    }
    if (!language.ttsSupported) return false
    return tts.speak(item.text(language), language.bcp47)
  }

  private suspend fun loadClip(language: Language, wordId: String): ByteArray? =
    try {
      Res.readBytes("files/audio/${language.code}_$wordId.m4a")
    } catch (e: Exception) {
      null
    }
}
