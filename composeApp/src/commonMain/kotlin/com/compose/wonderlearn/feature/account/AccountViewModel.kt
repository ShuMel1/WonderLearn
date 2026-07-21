package com.compose.wonderlearn.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.Profile
import com.compose.wonderlearn.domain.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountState(
  val profiles: List<Profile> = emptyList(),
  val activeProfileId: String? = null,
) {
  val activeProfile: Profile? get() = profiles.firstOrNull { it.id == activeProfileId }
}

class AccountViewModel(
  private val profileRepository: ProfileRepository,
  private val languagePreferences: LanguagePreferences,
) : ViewModel() {

  val state: StateFlow<AccountState> =
    combine(
      profileRepository.profiles(),
      profileRepository.activeProfileId(),
    ) { profiles, activeId -> AccountState(profiles, activeId) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountState())

  fun switchProfile(id: String) {
    viewModelScope.launch { profileRepository.setActiveProfile(id) }
  }

  fun addChild(displayName: String) {
    val name = displayName.trim()
    if (name.isEmpty()) return
    viewModelScope.launch {
      val profile = profileRepository.createProfile(name)
      profileRepository.setActiveProfile(profile.id)
    }
  }

  fun chooseLanguage(language: Language) {
    viewModelScope.launch { languagePreferences.setLanguage(language) }
  }
}
