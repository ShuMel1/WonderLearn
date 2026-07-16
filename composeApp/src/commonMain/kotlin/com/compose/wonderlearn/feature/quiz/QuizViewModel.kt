package com.compose.wonderlearn.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.speech.TextToSpeaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class QuizState(
  val options: List<VocabularyItem> = emptyList(),
  val target: VocabularyItem? = null,
  val wrongId: String? = null,
  val solved: Boolean = false,
  val score: Int = 0,
  val round: Int = 0,
)

class QuizViewModel(
  private val repository: VocabularyRepository,
  private val speaker: TextToSpeaker,
) : ViewModel() {

  private val _state = MutableStateFlow(QuizState())
  val state: StateFlow<QuizState> = _state.asStateFlow()

  init {
    nextRound()
  }

  fun nextRound() {
    viewModelScope.launch {
      val category = repository.categories().first().random()
      val options = repository.itemsForCategory(category.id).first().shuffled().take(4)
      val target = options.random()
      _state.value = _state.value.copy(
        options = options,
        target = target,
        wrongId = null,
        solved = false,
        round = _state.value.round + 1,
      )
      speaker.speak(target.text(Language.ENGLISH), Language.ENGLISH.bcp47)
    }
  }

  fun onSelect(item: VocabularyItem) {
    val current = _state.value
    if (current.solved) return
    val target = current.target ?: return
    if (item.id == target.id) {
      _state.value = current.copy(solved = true, wrongId = null, score = current.score + 1)
      viewModelScope.launch {
        delay(1100)
        nextRound()
      }
    } else {
      _state.value = current.copy(wrongId = item.id)
    }
  }

  fun replay(language: Language): Boolean {
    val target = _state.value.target ?: return false
    return speaker.speak(target.text(language), language.bcp47)
  }
}
