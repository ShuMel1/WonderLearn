package com.compose.wonderlearn.feature.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WordListViewModel(
  val categoryId: String,
  repository: VocabularyRepository,
) : ViewModel() {

  val items: StateFlow<List<VocabularyItem>> = repository.itemsForCategory(categoryId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
