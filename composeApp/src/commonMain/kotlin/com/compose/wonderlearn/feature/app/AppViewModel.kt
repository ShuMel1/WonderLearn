package com.compose.wonderlearn.feature.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.data.content.ContentSeeder
import com.compose.wonderlearn.data.content.ContentSource
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppState(
  val loading: Boolean = true,
  val nativeLanguage: Language? = null,
  val targetLanguage: Language? = null,
)

class AppViewModel(
  preferences: LanguagePreferences,
  contentSeeder: ContentSeeder,
  bundledContent: ContentSource,
  remoteContent: ContentSource,
) : ViewModel() {

  private val contentReady = MutableStateFlow(false)

  init {
    viewModelScope.launch {
      if (!contentSeeder.hasContent()) contentSeeder.sync(bundledContent)
      contentReady.value = true
      contentSeeder.sync(remoteContent)
    }
  }

  val state: StateFlow<AppState> =
    combine(
      preferences.nativeLanguage(),
      preferences.targetLanguage(),
      contentReady,
    ) { native, target, ready ->
      AppState(loading = !ready, nativeLanguage = native, targetLanguage = target)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState(loading = true))
}
