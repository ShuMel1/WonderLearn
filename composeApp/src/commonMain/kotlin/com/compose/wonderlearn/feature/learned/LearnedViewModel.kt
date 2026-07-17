package com.compose.wonderlearn.feature.learned

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.LearningRepository
import com.compose.wonderlearn.domain.VocabularyItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LearnedViewModel(
  learning: LearningRepository,
) : ViewModel() {

  val items: StateFlow<List<VocabularyItem>> = learning.learnedItems()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
