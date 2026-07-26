package com.compose.wonderlearn.feature.bubblepop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val BUBBLE_COUNT = 4
private val COLUMNS = listOf(0.12f, 0.38f, 0.62f, 0.88f)

data class Bubble(
  val id: Int,
  val item: VocabularyItem,
  val x: Float,
)

data class BubblePopState(
  val bubbles: List<Bubble> = emptyList(),
  val targetId: String = "",
  val targetText: String = "",
  val poppedWrong: Set<Int> = emptySet(),
  val score: Int = 0,
  val roundKey: Int = 0,
  val loading: Boolean = true,
)

class BubblePopViewModel(
  private val vocabulary: VocabularyRepository,
  private val progress: ProgressRepository,
  private val pronouncer: Pronouncer,
  private val preferences: LanguagePreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(BubblePopState())
  val state: StateFlow<BubblePopState> = _state.asStateFlow()

  private var language: Language? = null
  private var nextId = 0

  init {
    newRound()
  }

  fun newRound() {
    viewModelScope.launch {
      val lang = language ?: preferences.targetLanguage().filterNotNull().first().also { language = it }
      val words = vocabulary.randomItems(80).distinctBy { it.id }.take(BUBBLE_COUNT)
      if (words.size < BUBBLE_COUNT) {
        _state.value = _state.value.copy(loading = false)
        return@launch
      }
      val target = words.random()
      val columns = COLUMNS.shuffled()
      val bubbles = words.mapIndexed { index, word -> Bubble(nextId++, word, columns[index]) }
      _state.value = _state.value.copy(
        bubbles = bubbles,
        targetId = target.id,
        targetText = target.text(lang),
        poppedWrong = emptySet(),
        roundKey = _state.value.roundKey + 1,
        loading = false,
      )
      pronouncer.pronounce(target, lang)
    }
  }

  fun onPop(bubble: Bubble) {
    val current = _state.value
    if (bubble.id in current.poppedWrong) return
    if (bubble.item.id == current.targetId) {
      _state.value = current.copy(score = current.score + 1)
      viewModelScope.launch { progress.recordCorrectAnswer() }
      newRound()
    } else {
      _state.value = current.copy(poppedWrong = current.poppedWrong + bubble.id)
    }
  }

  fun onEscaped() {
    newRound()
  }

  fun replay() {
    val lang = language ?: return
    val target = _state.value.bubbles.firstOrNull { it.item.id == _state.value.targetId } ?: return
    viewModelScope.launch { pronouncer.pronounce(target.item, lang) }
  }
}
