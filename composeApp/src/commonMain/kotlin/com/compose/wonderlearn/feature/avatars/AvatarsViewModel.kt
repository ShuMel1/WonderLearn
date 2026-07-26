package com.compose.wonderlearn.feature.avatars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.AvatarItem
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.RewardsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AvatarsState(
  val coins: Int = 0,
  val unlocked: Set<String> = emptySet(),
  val current: String? = null,
)

class AvatarsViewModel(
  private val rewards: RewardsRepository,
  private val profiles: ProfileRepository,
) : ViewModel() {

  private val currentAvatar =
    combine(profiles.profiles(), profiles.activeProfileId()) { list, activeId ->
      list.firstOrNull { it.id == activeId }?.avatarId
    }

  val state: StateFlow<AvatarsState> =
    combine(rewards.coins(), rewards.unlockedAvatars(), currentAvatar) { coins, unlocked, current ->
      AvatarsState(coins = coins, unlocked = unlocked, current = current)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AvatarsState())

  fun onAvatarClick(avatar: AvatarItem) {
    viewModelScope.launch {
      val available = avatar.price == 0 || avatar.emoji in state.value.unlocked
      if (available) {
        profiles.setAvatar(profiles.currentProfileId(), avatar.emoji)
      } else if (rewards.unlockAvatar(avatar.emoji, avatar.price)) {
        profiles.setAvatar(profiles.currentProfileId(), avatar.emoji)
      }
    }
  }
}
