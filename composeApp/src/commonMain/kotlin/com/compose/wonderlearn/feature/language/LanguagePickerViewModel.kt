package com.compose.wonderlearn.feature.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import kotlinx.coroutines.launch

enum class LanguageRole { NATIVE, TARGET }

class LanguagePickerViewModel(
  private val preferences: LanguagePreferences,
) : ViewModel() {

  fun choose(language: Language, role: LanguageRole) {
    viewModelScope.launch {
      when (role) {
        LanguageRole.NATIVE -> preferences.setNativeLanguage(language)
        LanguageRole.TARGET -> preferences.setTargetLanguage(language)
      }
    }
  }
}
