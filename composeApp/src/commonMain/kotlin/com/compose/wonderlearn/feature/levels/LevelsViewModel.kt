package com.compose.wonderlearn.feature.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.AnswerBus
import com.compose.wonderlearn.domain.DailyAdventureRepository
import com.compose.wonderlearn.domain.LevelDef
import com.compose.wonderlearn.domain.LevelKind
import com.compose.wonderlearn.domain.LevelRunController
import com.compose.wonderlearn.domain.LevelsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LevelStatus { LOCKED, CURRENT, DONE }

data class LevelNode(
  val def: LevelDef,
  val status: LevelStatus,
)

data class LevelsUiState(
  val nodes: List<LevelNode> = emptyList(),
  val loading: Boolean = true,
)

class LevelsViewModel(
  private val levels: LevelsRepository,
  private val dailyAdventure: DailyAdventureRepository,
  private val answerBus: AnswerBus,
  private val runController: LevelRunController,
) : ViewModel() {

  private val todaysLevels = dailyAdventure.todaysLevels()

  private val _state = MutableStateFlow(LevelsUiState())
  val state: StateFlow<LevelsUiState> = _state.asStateFlow()

  private val _justCompleted = MutableStateFlow<String?>(null)
  val justCompleted: StateFlow<String?> = _justCompleted.asStateFlow()

  /** Fires once, the moment today's path is fully cleared and its reward is claimed. */
  private val _rewardEarned = MutableStateFlow(false)
  val rewardEarned: StateFlow<Boolean> = _rewardEarned.asStateFlow()

  init {
    viewModelScope.launch {
      dailyAdventure.completedToday().collect { completed ->
        _state.value = LevelsUiState(nodes = buildNodes(completed), loading = false)
      }
    }
    viewModelScope.launch {
      answerBus.events.collect { correct -> handleAnswer(correct) }
    }
    viewModelScope.launch {
      answerBus.finished.collect { handleFinished() }
    }
  }

  private suspend fun handleAnswer(correct: Boolean) {
    val def = activeLevel() ?: return
    if (def.kind == LevelKind.MEMORY) return
    if (!correct) {
      runController.onWrong()
      return
    }
    if (runController.onCorrect()) complete(def)
  }

  private suspend fun handleFinished() {
    val def = activeLevel() ?: return
    if (def.kind == LevelKind.MEMORY) complete(def)
  }

  private fun activeLevel(): LevelDef? {
    val activeId = runController.activeLevelId ?: return null
    return todaysLevels.firstOrNull { it.id == activeId }
  }

  private suspend fun complete(def: LevelDef) {
    levels.markComplete(def.id)
    dailyAdventure.markLevelDone(def.id)
    runController.markCompleted(def.id)
    _justCompleted.value = def.id
    if (dailyAdventure.claimReward()) _rewardEarned.value = true
  }

  private fun buildNodes(completed: Set<String>): List<LevelNode> =
    todaysLevels.mapIndexed { position, def ->
      val status = when {
        def.id in completed -> LevelStatus.DONE
        position == 0 || todaysLevels[position - 1].id in completed -> LevelStatus.CURRENT
        else -> LevelStatus.LOCKED
      }
      LevelNode(def, status)
    }

  fun onStart(def: LevelDef) {
    runController.begin(def.id, def.answersToWin)
  }

  fun clearCompleted() {
    _justCompleted.value = null
    runController.consumeCompleted()
  }

  fun clearRewardEarned() {
    _rewardEarned.value = false
  }
}
