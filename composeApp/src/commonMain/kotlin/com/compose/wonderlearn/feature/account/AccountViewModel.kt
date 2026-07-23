package com.compose.wonderlearn.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.Profile
import com.compose.wonderlearn.domain.DEFAULT_DAILY_GOAL
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.ProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountState(
  val profiles: List<Profile> = emptyList(),
  val activeProfileId: String? = null,
  val dailyGoal: Int = DEFAULT_DAILY_GOAL,
) {
  val activeProfile: Profile? get() = profiles.firstOrNull { it.id == activeProfileId }
}

class AccountViewModel(
  private val profileRepository: ProfileRepository,
  private val languagePreferences: LanguagePreferences,
  private val progressRepository: ProgressRepository,
) : ViewModel() {

  val state: StateFlow<AccountState> =
    combine(
      profileRepository.profiles(),
      profileRepository.activeProfileId(),
      progressRepository.dailyGoal(),
    ) { profiles, activeId, goal -> AccountState(profiles, activeId, goal) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountState())

  fun setDailyGoal(goal: Int) {
    viewModelScope.launch { progressRepository.setDailyGoal(goal) }
  }

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
