package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LevelKind {
  LEARN,
  MEMORY,
  BUBBLE_POP,
  ODD_ONE_OUT,
}

enum class GameSize {
  EASY,
  MEDIUM,
  HARD,
}

data class LevelDef(
  val id: String,
  val index: Int,
  val kind: LevelKind,
  val answersToWin: Int = 0,
  val size: GameSize? = null,
)

val LEVELS: List<LevelDef> = listOf(
  LevelDef("1", 1, LevelKind.LEARN, answersToWin = 3),
  LevelDef("2", 2, LevelKind.MEMORY, size = GameSize.EASY),
  LevelDef("3", 3, LevelKind.BUBBLE_POP, answersToWin = 2),
  LevelDef("4", 4, LevelKind.LEARN, answersToWin = 3),
  LevelDef("5", 5, LevelKind.ODD_ONE_OUT, answersToWin = 3),
  LevelDef("6", 6, LevelKind.LEARN, answersToWin = 3),
  LevelDef("7", 7, LevelKind.MEMORY, size = GameSize.MEDIUM),
  LevelDef("8", 8, LevelKind.BUBBLE_POP, answersToWin = 4),
  LevelDef("9", 9, LevelKind.LEARN, answersToWin = 4),
  LevelDef("10", 10, LevelKind.ODD_ONE_OUT, answersToWin = 4),
  LevelDef("11", 11, LevelKind.MEMORY, size = GameSize.HARD),
  LevelDef("12", 12, LevelKind.BUBBLE_POP, answersToWin = 5),
)

interface LevelsRepository {
  fun completedLevels(): Flow<Set<String>>

  suspend fun markComplete(levelId: String)
}

data class LevelRun(
  val levelId: String,
  val goal: Int,
)

class LevelRunController {
  private val _active = MutableStateFlow<LevelRun?>(null)
  val active: StateFlow<LevelRun?> = _active.asStateFlow()

  private val _streak = MutableStateFlow(0)
  val streak: StateFlow<Int> = _streak.asStateFlow()

  private val _completed = MutableStateFlow<String?>(null)
  val completed: StateFlow<String?> = _completed.asStateFlow()

  val activeLevelId: String? get() = _active.value?.levelId

  fun begin(levelId: String, goal: Int) {
    _active.value = LevelRun(levelId, goal)
    _streak.value = 0
    _completed.value = null
  }

  fun onCorrect(): Boolean {
    val run = _active.value ?: return false
    _streak.value = _streak.value + 1
    return _streak.value >= run.goal
  }

  fun onWrong() {
    if (_active.value != null) _streak.value = 0
  }

  fun markCompleted(levelId: String) {
    _active.value = null
    _streak.value = 0
    _completed.value = levelId
  }

  fun consumeCompleted() {
    _completed.value = null
  }

  fun clear() {
    _active.value = null
    _streak.value = 0
    _completed.value = null
  }
}
