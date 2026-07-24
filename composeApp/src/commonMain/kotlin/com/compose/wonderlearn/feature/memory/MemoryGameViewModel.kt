package com.compose.wonderlearn.feature.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val PAIR_COUNT = 6
private const val MISMATCH_DELAY_MS = 800L

data class MemoryCard(
  val cardId: Int,
  val item: VocabularyItem,
  val revealed: Boolean = false,
  val matched: Boolean = false,
) {
  val wordId: String get() = item.id
  val emoji: String get() = item.emoji
  val imageRef: String? get() = item.imageRef
}

data class MemoryState(
  val cards: List<MemoryCard> = emptyList(),
  val loading: Boolean = true,
  val moves: Int = 0,
  val matchedPairs: Int = 0,
  val totalPairs: Int = 0,
) {
  val won: Boolean get() = totalPairs > 0 && matchedPairs == totalPairs
}

class MemoryGameViewModel(
  private val vocabulary: VocabularyRepository,
  private val progress: ProgressRepository,
  private val pronouncer: Pronouncer,
  private val preferences: LanguagePreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(MemoryState())
  val state: StateFlow<MemoryState> = _state.asStateFlow()

  private var firstPick: Int? = null
  private var busy = false
  private var language: Language? = null

  init {
    newGame()
  }

  fun newGame() {
    viewModelScope.launch {
      _state.value = MemoryState(loading = true)
      firstPick = null
      busy = false
      val words = vocabulary.randomItems(PAIR_COUNT)
      var nextId = 0
      val cards = words.flatMap { word ->
        List(2) { MemoryCard(cardId = nextId++, item = word) }
      }.shuffled()
      _state.value = MemoryState(cards = cards, loading = false, totalPairs = words.size)
    }
  }

  fun onCardClick(cardId: Int) {
    if (busy) return
    val card = _state.value.cards.firstOrNull { it.cardId == cardId } ?: return
    if (card.revealed || card.matched) return

    setRevealed(cardId, true)
    pronounce(card.item)
    val first = firstPick
    if (first == null) {
      firstPick = cardId
      return
    }

    _state.value = _state.value.copy(moves = _state.value.moves + 1)
    val firstCard = _state.value.cards.first { it.cardId == first }
    if (firstCard.wordId == card.wordId) {
      setMatched(first, cardId)
      firstPick = null
      _state.value = _state.value.copy(matchedPairs = _state.value.matchedPairs + 1)
      viewModelScope.launch { progress.recordCorrectAnswer() }
    } else {
      busy = true
      viewModelScope.launch {
        delay(MISMATCH_DELAY_MS)
        setRevealed(first, false)
        setRevealed(cardId, false)
        firstPick = null
        busy = false
      }
    }
  }

  private fun pronounce(item: VocabularyItem) {
    viewModelScope.launch {
      val lang = language ?: preferences.targetLanguage().filterNotNull().first().also { language = it }
      pronouncer.pronounce(item, lang)
    }
  }

  private fun setRevealed(cardId: Int, revealed: Boolean) {
    _state.value = _state.value.copy(
      cards = _state.value.cards.map { if (it.cardId == cardId) it.copy(revealed = revealed) else it },
    )
  }

  private fun setMatched(vararg cardIds: Int) {
    val ids = cardIds.toSet()
    _state.value = _state.value.copy(
      cards = _state.value.cards.map {
        if (it.cardId in ids) it.copy(matched = true, revealed = true) else it
      },
    )
  }
}
