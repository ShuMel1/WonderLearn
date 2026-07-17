package com.compose.wonderlearn.feature.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import kotlinx.coroutines.launch

class LanguagePickerViewModel(
  private val preferences: LanguagePreferences,
) : ViewModel() {

  fun choose(language: Language) {
    viewModelScope.launch {
      preferences.setLanguage(language)
    }
  }
}
