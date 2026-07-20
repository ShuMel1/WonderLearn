package com.compose.wonderlearn.feature.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordListViewModel(
  val categoryId: String,
  repository: VocabularyRepository,
  private val pronouncer: Pronouncer,
) : ViewModel() {

  val items: StateFlow<List<VocabularyItem>> = repository.itemsForCategory(categoryId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  private val _playingId = MutableStateFlow<String?>(null)
  val playingId: StateFlow<String?> = _playingId.asStateFlow()

  private val _unavailable = Channel<Unit>(Channel.BUFFERED)
  val unavailable = _unavailable.receiveAsFlow()

  private var playToken = 0

  fun play(item: VocabularyItem, language: Language) {
    val token = ++playToken
    _playingId.value = item.id
    viewModelScope.launch {
      try {
        if (!pronouncer.pronounce(item, language)) _unavailable.send(Unit)
      } finally {
        if (token == playToken) _playingId.value = null
      }
    }
  }
}
