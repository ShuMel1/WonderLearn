package com.compose.wonderlearn.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.DailyAdventureRepository
import com.compose.wonderlearn.domain.DailyProgress
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.RewardsRepository
import com.compose.wonderlearn.domain.checkInRewardForPosition
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
  progress: ProgressRepository,
  private val rewards: RewardsRepository,
  dailyAdventure: DailyAdventureRepository,
) : ViewModel() {

  val dailyProgress: StateFlow<DailyProgress> =
    progress.dailyProgress()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyProgress())

  val gold: StateFlow<Int> =
    rewards.gold().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

  val gems: StateFlow<Int> =
    rewards.gems().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

  val checkInLadderPosition: StateFlow<Int> =
    rewards.checkInLadderPosition().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

  val checkInRewardToday: StateFlow<Int> =
    checkInLadderPosition.map { checkInRewardForPosition(it) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), checkInRewardForPosition(1))

  // Defaults to true (already checked in) so the popup never flashes on the loading frame before
  // real data arrives — worst case it appears a beat late, never falsely early.
  val checkedInToday: StateFlow<Boolean> =
    rewards.checkedInToday().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

  val adventureDoneToday: StateFlow<Boolean> =
    dailyAdventure.rewardClaimed().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

  fun claimCheckIn() {
    viewModelScope.launch { rewards.claimDailyCheckIn() }
  }
}
