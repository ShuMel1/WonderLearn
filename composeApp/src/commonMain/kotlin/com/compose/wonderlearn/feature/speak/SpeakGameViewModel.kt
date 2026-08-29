package com.compose.wonderlearn.feature.speak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.AnswerBus
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.speech.PronunciationMatcher
import com.compose.wonderlearn.speech.SpeechOutcome
import com.compose.wonderlearn.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SpeakPhase { LOADING, READY, LISTENING, CORRECT, TRY_AGAIN, UNAVAILABLE }

data class SpeakState(
  val word: VocabularyItem? = null,
  val phase: SpeakPhase = SpeakPhase.LOADING,
  val prompt: String = "",
  val heard: String? = null,
  val score: Int = 0,
  val speaking: Boolean = false,
)

class SpeakGameViewModel(
  private val vocabulary: VocabularyRepository,
  private val recognizer: SpeechRecognizer,
  private val pronouncer: Pronouncer,
  private val preferences: LanguagePreferences,
  private val progress: ProgressRepository,
  private val answerBus: AnswerBus,
) : ViewModel() {

  private val _state = MutableStateFlow(SpeakState())
  val state: StateFlow<SpeakState> = _state.asStateFlow()

  private var language: Language? = null
  private var pool: List<VocabularyItem> = emptyList()
  private var index = 0

  init {
    viewModelScope.launch {
      val target = preferences.targetLanguage().filterNotNull().first().also { language = it }
      if (!target.asrSupported || !recognizer.isAvailable(target.bcp47)) {
        _state.value = _state.value.copy(phase = SpeakPhase.UNAVAILABLE)
        return@launch
      }
      pool = vocabulary.randomItems(POOL_SIZE).shuffled()
      if (pool.isEmpty()) {
        _state.value = _state.value.copy(phase = SpeakPhase.UNAVAILABLE)
        return@launch
      }
      present(pool.first())
    }
  }

  fun listen() {
    val current = _state.value
    val word = current.word ?: return
    val target = language ?: return
    if (current.phase == SpeakPhase.LISTENING) return
    pronouncer.stop()
    _state.value = current.copy(phase = SpeakPhase.LISTENING, heard = null, speaking = false)
    viewModelScope.launch {
      when (val outcome = recognizer.listen(target.bcp47)) {
        is SpeechOutcome.Heard -> score(word, target, outcome.transcripts)
        SpeechOutcome.NoSpeech -> _state.value = _state.value.copy(phase = SpeakPhase.TRY_AGAIN, heard = null)
        SpeechOutcome.Error -> _state.value = _state.value.copy(phase = SpeakPhase.TRY_AGAIN, heard = null)
        SpeechOutcome.Unavailable -> _state.value = _state.value.copy(phase = SpeakPhase.UNAVAILABLE)
      }
    }
  }

  fun replay() {
    val word = _state.value.word ?: return
    if (_state.value.speaking) return
    viewModelScope.launch { speak(word) }
  }

  fun next() {
    if (pool.isEmpty()) return
    index = (index + 1) % pool.size
    if (index == 0) pool = pool.shuffled()
    present(pool[index])
  }

  private fun score(word: VocabularyItem, target: Language, transcripts: List<String>) {
    val expected = word.text(target)
    if (PronunciationMatcher.matches(expected, transcripts)) {
      _state.value = _state.value.copy(
        phase = SpeakPhase.CORRECT,
        heard = transcripts.firstOrNull(),
        score = _state.value.score + 1,
      )
      answerBus.report(true)
      viewModelScope.launch { progress.recordCorrectAnswer() }
    } else {
      _state.value = _state.value.copy(phase = SpeakPhase.TRY_AGAIN, heard = transcripts.firstOrNull())
      answerBus.report(false)
    }
  }

  private fun present(word: VocabularyItem) {
    val target = language ?: return
    _state.value = _state.value.copy(
      word = word,
      phase = SpeakPhase.READY,
      prompt = word.text(target),
      heard = null,
    )
    viewModelScope.launch { speak(word) }
  }

  private suspend fun speak(word: VocabularyItem) {
    val target = language ?: return
    _state.value = _state.value.copy(speaking = true)
    try {
      pronouncer.pronounce(word, target)
    } finally {
      _state.value = _state.value.copy(speaking = false)
    }
  }

  override fun onCleared() {
    recognizer.cancel()
  }

  private companion object {
    const val POOL_SIZE = 60
  }
}
