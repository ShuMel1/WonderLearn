package com.compose.wonderlearn.feature.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AppState(
  val loading: Boolean = true,
  val language: Language? = null,
)

class AppViewModel(
  preferences: LanguagePreferences,
) : ViewModel() {

  val state: StateFlow<AppState> = preferences.selectedLanguage()
    .map { AppState(loading = false, language = it) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState(loading = true))
}
