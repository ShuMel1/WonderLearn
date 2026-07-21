package com.compose.wonderlearn.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class WordDetailState(
  val siblings: List<VocabularyItem> = emptyList(),
  val index: Int = 0,
  val speaking: Boolean = false,
) {
  val item: VocabularyItem? get() = siblings.getOrNull(index)
  val hasSiblings: Boolean get() = siblings.size > 1
}

class WordDetailViewModel(
  private val itemId: String,
  private val repository: VocabularyRepository,
  private val pronouncer: Pronouncer,
) : ViewModel() {

  private val _state = MutableStateFlow(WordDetailState())
  val state: StateFlow<WordDetailState> = _state.asStateFlow()

  private val _unavailable = Channel<Unit>(Channel.BUFFERED)
  val unavailable = _unavailable.receiveAsFlow()

  private var speakToken = 0

  init {
    viewModelScope.launch {
      val opened = repository.item(itemId) ?: return@launch
      val siblings = repository.itemsForCategory(opened.categoryId).first()
      val index = siblings.indexOfFirst { it.id == opened.id }
      _state.value = if (index >= 0) {
        WordDetailState(siblings = siblings, index = index)
      } else {
        WordDetailState(siblings = listOf(opened), index = 0)
      }
    }
  }

  fun pronounce(language: Language) {
    if (_state.value.speaking) return
    speak(language)
  }

  fun next(language: Language) = move(1, language)

  fun previous(language: Language) = move(-1, language)

  private fun move(step: Int, language: Language) {
    val current = _state.value
    val size = current.siblings.size
    if (size == 0) return
    val target = ((current.index + step) % size + size) % size
    _state.value = current.copy(index = target)
    speak(language)
  }

  private fun speak(language: Language) {
    val item = _state.value.item ?: return
    val token = ++speakToken
    _state.value = _state.value.copy(speaking = true)
    viewModelScope.launch {
      try {
        if (!pronouncer.pronounce(item, language)) _unavailable.send(Unit)
      } finally {
        if (token == speakToken) _state.value = _state.value.copy(speaking = false)
      }
    }
  }
}
