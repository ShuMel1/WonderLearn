package com.compose.wonderlearn.feature.learned

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.LearningRepository
import com.compose.wonderlearn.domain.VocabularyItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class LearnedViewModel(
  learning: LearningRepository,
  preferences: LanguagePreferences,
) : ViewModel() {

  val items: StateFlow<List<VocabularyItem>> = preferences.targetLanguage()
    .filterNotNull()
    .flatMapLatest { language -> learning.learnedItems(language) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
