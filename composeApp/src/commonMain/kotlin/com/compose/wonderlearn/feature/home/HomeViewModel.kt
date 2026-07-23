package com.compose.wonderlearn.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.DailyProgress
import com.compose.wonderlearn.domain.ProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
  progress: ProgressRepository,
) : ViewModel() {

  val dailyProgress: StateFlow<DailyProgress> =
    progress.dailyProgress()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyProgress())
}
