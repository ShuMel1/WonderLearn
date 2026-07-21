package com.compose.wonderlearn.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.LearningRepository
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.QuizMode
import com.compose.wonderlearn.domain.VocabularyItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class QuizState(
  val options: List<VocabularyItem> = emptyList(),
  val target: VocabularyItem? = null,
  val wrongId: String? = null,
  val solved: Boolean = false,
  val justLearned: Boolean = false,
  val allLearned: Boolean = false,
  val score: Int = 0,
  val loading: Boolean = true,
  val speaking: Boolean = false,
  val mode: QuizMode = QuizMode.LEARN,
)

class QuizViewModel(
  private val learning: LearningRepository,
  private val pronouncer: Pronouncer,
  private val preferences: LanguagePreferences,
  private val mode: QuizMode = QuizMode.LEARN,
) : ViewModel() {

  private val _state = MutableStateFlow(QuizState(mode = mode))
  val state: StateFlow<QuizState> = _state.asStateFlow()

  private val _unavailable = Channel<Unit>(Channel.BUFFERED)
  val unavailable = _unavailable.receiveAsFlow()

  private var language: Language? = null

  init {
    nextRound()
  }

  private suspend fun awaitLanguage(): Language =
    language ?: (preferences.targetLanguage().filterNotNull().first()).also { language = it }

  fun nextRound() {
    viewModelScope.launch {
      val language = awaitLanguage()
      val round = learning.nextRound(language, mode)
      if (round == null) {
        _state.value = _state.value.copy(target = null, allLearned = true, loading = false)
        return@launch
      }
      _state.value = _state.value.copy(
        options = round.options,
        target = round.target,
        wrongId = null,
        solved = false,
        justLearned = false,
        allLearned = false,
        loading = false,
      )
      speak(round.target)
    }
  }

  fun onSelect(item: VocabularyItem) {
    val current = _state.value
    if (current.solved || current.allLearned) return
    val target = current.target ?: return
    if (item.id == target.id) {
      viewModelScope.launch {
        val nowLearned =
          learning.recordCorrect(target.id, awaitLanguage()) && mode == QuizMode.LEARN
        _state.value = _state.value.copy(
          solved = true,
          wrongId = null,
          justLearned = nowLearned,
          score = _state.value.score + 1,
        )
        delay(if (nowLearned) 1600 else 1000)
        nextRound()
      }
    } else {
      _state.value = current.copy(wrongId = item.id)
      viewModelScope.launch { learning.recordWrong(target.id, awaitLanguage(), mode) }
    }
  }

  fun replay() {
    val current = _state.value
    val target = current.target ?: return
    if (current.speaking) return
    viewModelScope.launch { speak(target) }
  }

  private suspend fun speak(target: VocabularyItem) {
    _state.value = _state.value.copy(speaking = true)
    try {
      if (!pronouncer.pronounce(target, awaitLanguage())) _unavailable.send(Unit)
    } finally {
      _state.value = _state.value.copy(speaking = false)
    }
  }
}
