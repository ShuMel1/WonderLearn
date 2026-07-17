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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class WordDetailState(
  val item: VocabularyItem? = null,
)

class WordDetailViewModel(
  private val itemId: String,
  private val repository: VocabularyRepository,
  private val pronouncer: Pronouncer,
) : ViewModel() {

  private val _state = MutableStateFlow(WordDetailState())
  val state: StateFlow<WordDetailState> = _state.asStateFlow()

  private val _unavailable = Channel<Unit>(Channel.BUFFERED)
  val unavailable = _unavailable.receiveAsFlow()

  init {
    viewModelScope.launch {
      _state.value = _state.value.copy(item = repository.item(itemId))
    }
  }

  fun pronounce(language: Language) {
    val item = _state.value.item ?: return
    viewModelScope.launch {
      if (!pronouncer.pronounce(item, language)) _unavailable.send(Unit)
    }
  }
}
