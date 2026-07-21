package com.compose.wonderlearn.feature.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AppState(
  val loading: Boolean = true,
  val nativeLanguage: Language? = null,
  val targetLanguage: Language? = null,
)

class AppViewModel(
  preferences: LanguagePreferences,
) : ViewModel() {

  val state: StateFlow<AppState> =
    combine(
      preferences.nativeLanguage(),
      preferences.targetLanguage(),
    ) { native, target ->
      AppState(loading = false, nativeLanguage = native, targetLanguage = target)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState(loading = true))
}
