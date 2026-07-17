package com.compose.wonderlearn.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LearningRepository
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.speech.TextToSpeaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
)

class QuizViewModel(
  private val learning: LearningRepository,
  private val speaker: TextToSpeaker,
) : ViewModel() {

  private val _state = MutableStateFlow(QuizState())
  val state: StateFlow<QuizState> = _state.asStateFlow()

  init {
    nextRound()
  }

  fun nextRound() {
    viewModelScope.launch {
      val round = learning.nextRound()
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
      speaker.speak(round.target.text(Language.ENGLISH), Language.ENGLISH.bcp47)
    }
  }

  fun onSelect(item: VocabularyItem) {
    val current = _state.value
    if (current.solved || current.allLearned) return
    val target = current.target ?: return
    if (item.id == target.id) {
      viewModelScope.launch {
        val nowLearned = learning.recordCorrect(target.id)
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
      viewModelScope.launch { learning.recordWrong(target.id) }
    }
  }

  fun replay(language: Language): Boolean {
    val target = _state.value.target ?: return false
    return speaker.speak(target.text(language), language.bcp47)
  }
}
