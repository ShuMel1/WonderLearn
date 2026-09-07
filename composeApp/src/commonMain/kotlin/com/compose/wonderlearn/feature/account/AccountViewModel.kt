package com.compose.wonderlearn.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.FREE_AVATARS
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.Profile
import com.compose.wonderlearn.domain.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountState(
  val profiles: List<Profile> = emptyList(),
  val activeProfileId: String? = null,
) {
  val activeProfile: Profile? get() = profiles.firstOrNull { it.id == activeProfileId }
}

/**
 * Shared across the account menu and its sub-pages (Languages, Manage Kids) — each screen pulls
 * its own [koinViewModel] instance, all backed by the same repositories, so there's no need to
 * hand-carry state between destinations.
 */
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
      // Read the outgoing active profile's language before switching, so the new child doesn't
      // land on a null language pair (which would otherwise re-trigger the "which language do you
      // speak" onboarding picker the moment they're switched to).
      val inheritedNative = languagePreferences.nativeLanguage().first()
      val inheritedTarget = languagePreferences.targetLanguage().first()
      val profile = profileRepository.createProfile(name, FREE_AVATARS.random())
      profileRepository.setActiveProfile(profile.id)
      inheritedNative?.let { languagePreferences.setNativeLanguage(it) }
      inheritedTarget?.let { languagePreferences.setTargetLanguage(it) }
    }
  }

  fun renameProfile(id: String, displayName: String) {
    viewModelScope.launch { profileRepository.renameProfile(id, displayName) }
  }

  fun deleteProfile(id: String) {
    viewModelScope.launch { profileRepository.deleteProfile(id) }
  }

  fun chooseTargetLanguage(language: Language) {
    viewModelScope.launch { languagePreferences.setTargetLanguage(language) }
  }

  fun chooseNativeLanguage(language: Language) {
    viewModelScope.launch { languagePreferences.setNativeLanguage(language) }
  }
}
