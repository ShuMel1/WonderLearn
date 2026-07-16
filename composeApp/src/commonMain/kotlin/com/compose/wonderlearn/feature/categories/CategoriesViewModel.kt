package com.compose.wonderlearn.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CategoriesViewModel(
  repository: VocabularyRepository,
) : ViewModel() {

  val categories: StateFlow<List<Category>> = repository.categories()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
