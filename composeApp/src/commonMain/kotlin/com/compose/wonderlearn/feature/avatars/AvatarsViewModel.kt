package com.compose.wonderlearn.feature.avatars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.AvatarItem
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.RewardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AvatarsState(
  val gold: Int = 0,
  val gems: Int = 0,
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
    combine(rewards.gold(), rewards.gems(), rewards.unlockedAvatars(), currentAvatar) { gold, gems, unlocked, current ->
      AvatarsState(gold = gold, gems = gems, unlocked = unlocked, current = current)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AvatarsState())

  private val _exchangeFailed = MutableStateFlow(false)
  val exchangeFailed: StateFlow<Boolean> = _exchangeFailed.asStateFlow()

  private val _exchangeSucceeded = MutableStateFlow(false)
  val exchangeSucceeded: StateFlow<Boolean> = _exchangeSucceeded.asStateFlow()

  // Fires once a newly-*purchased* unlock lands (not one merely switched to, and not a purchase
  // that failed) — the Screen celebrates this, then calls consumeJustUnlocked().
  private val _justUnlocked = MutableStateFlow<String?>(null)
  val justUnlocked: StateFlow<String?> = _justUnlocked.asStateFlow()

  fun onAvatarClick(avatar: AvatarItem) {
    viewModelScope.launch {
      val available = avatar.price == 0 || avatar.emoji in state.value.unlocked
      if (available) {
        profiles.setAvatar(profiles.currentProfileId(), avatar.emoji)
      } else if (rewards.unlockAvatar(avatar.emoji, avatar.price)) {
        profiles.setAvatar(profiles.currentProfileId(), avatar.emoji)
        _justUnlocked.value = avatar.emoji
      }
    }
  }

  fun consumeJustUnlocked() {
    _justUnlocked.value = null
  }

  fun onExchangeClick() {
    viewModelScope.launch {
      if (rewards.exchangeGoldForGems()) {
        _exchangeSucceeded.value = true
      } else {
        _exchangeFailed.value = true
      }
    }
  }

  fun consumeExchangeFailed() {
    _exchangeFailed.value = false
  }

  fun consumeExchangeSucceeded() {
    _exchangeSucceeded.value = false
  }
}
