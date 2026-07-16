package com.compose.wonderlearn.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.speech.TextToSpeaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WordDetailState(
  val item: VocabularyItem? = null,
  val selectedLanguage: Language = Language.ENGLISH,
)

class WordDetailViewModel(
  private val itemId: String,
  private val repository: VocabularyRepository,
  private val speaker: TextToSpeaker,
) : ViewModel() {

  private val _state = MutableStateFlow(WordDetailState())
  val state: StateFlow<WordDetailState> = _state.asStateFlow()

  init {
    viewModelScope.launch {
      _state.value = _state.value.copy(item = repository.item(itemId))
    }
  }

  fun selectLanguage(language: Language) {
    _state.value = _state.value.copy(selectedLanguage = language)
  }

  fun speak(): Boolean {
    val current = _state.value
    val item = current.item ?: return false
    return speaker.speak(item.text(current.selectedLanguage), current.selectedLanguage.bcp47)
  }
}
