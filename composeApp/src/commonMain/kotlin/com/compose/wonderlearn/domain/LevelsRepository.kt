package com.compose.wonderlearn.domain

import kotlin.random.Random
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

/**
 * Today's 5-level slice of the level pool, deterministic for the day: seeding the shuffle with
 * the day itself means every device, on every platform, lands on the same 5 levels with no
 * backend and no stored selection — the day number *is* the selection.
 */
fun dailyAdventureLevels(day: Long, pool: List<LevelDef> = LEVELS, count: Int = 5): List<LevelDef> =
  pool.shuffled(Random(day)).take(count).sortedBy { it.index }

interface DailyAdventureRepository {
  /** Today's levels, in the order they should be played. */
  fun todaysLevels(): List<LevelDef>

  /** Which of today's levels have been completed today. Resets on its own once the day rolls over. */
  fun completedToday(): Flow<Set<String>>

  suspend fun markLevelDone(levelId: String)

  /** Whether today's completion reward has already been claimed. */
  fun rewardClaimed(): Flow<Boolean>

  /**
   * Claims today's reward if (and only if) every level in [todaysLevels] is done and it hasn't
   * been claimed yet. Returns `true` exactly once per day — this is the integration point for the
   * currency work to award Gems from; call it and credit Gems only when it returns `true`.
   */
  suspend fun claimReward(): Boolean
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
