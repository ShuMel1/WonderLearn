package com.compose.wonderlearn.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.DailyProgress
import com.compose.wonderlearn.domain.LevelsRepository
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.RewardsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
  progress: ProgressRepository,
  rewards: RewardsRepository,
  levels: LevelsRepository,
) : ViewModel() {

  val dailyProgress: StateFlow<DailyProgress> =
    progress.dailyProgress()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyProgress())

  val coins: StateFlow<Int> =
    rewards.coins().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

  val stars: StateFlow<Int> =
    levels.completedLevels().map { it.size }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
