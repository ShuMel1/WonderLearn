package com.compose.wonderlearn

import com.compose.wonderlearn.domain.AnswerBus
import com.compose.wonderlearn.domain.DailyAdventureRepository
import com.compose.wonderlearn.domain.LEVELS
import com.compose.wonderlearn.domain.LevelDef
import com.compose.wonderlearn.domain.LevelKind
import com.compose.wonderlearn.domain.LevelRunController
import com.compose.wonderlearn.domain.LevelsRepository
import com.compose.wonderlearn.domain.RewardsRepository
import com.compose.wonderlearn.feature.levels.LevelStatus
import com.compose.wonderlearn.feature.levels.LevelsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * These tests exercise LevelsViewModel wired to the full LEVELS pool as "today's levels" (via
 * the fake DailyAdventureRepository below), so the pre-existing progression assertions (level 1
 * unlocked first, etc.) stay meaningful. Day-scoped selection itself (dailyAdventureLevels) and
 * the SqlDelight-backed persistence/claim guarding are covered separately in
 * DailyAdventureRepositoryTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LevelsTest {

  private val dispatcher = StandardTestDispatcher()

  @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private val completed = MutableStateFlow<Set<String>>(emptySet())
  private val doneToday = MutableStateFlow<Set<String>>(emptySet())
  private val answerBus = AnswerBus()
  private val controller = LevelRunController()

  private val levels = object : LevelsRepository {
    override fun completedLevels(): Flow<Set<String>> = completed
    override suspend fun markComplete(levelId: String) { completed.value = completed.value + levelId }
  }

  private var rewardClaims = 0

  private val dailyAdventure = object : DailyAdventureRepository {
    override fun todaysLevels(): List<LevelDef> = LEVELS
    override fun completedToday(): Flow<Set<String>> = doneToday
    override suspend fun markLevelDone(levelId: String) { doneToday.value = doneToday.value + levelId }
    override fun rewardClaimed(): Flow<Boolean> = MutableStateFlow(rewardClaims > 0)
    override suspend fun claimReward(): Boolean {
      if (rewardClaims > 0 || doneToday.value.size < LEVELS.size) return false
      rewardClaims++
      return true
    }
  }

  private var gemsEarned = 0
  private val rewards = object : RewardsRepository {
    override fun gold(): Flow<Int> = flowOf(0)
    override fun gems(): Flow<Int> = flowOf(0)
    override suspend fun earnGold(amount: Int) = Unit
    override suspend fun earnGems(amount: Int) { gemsEarned += amount }
    override fun unlockedAvatars(): Flow<Set<String>> = flowOf(emptySet())
    override suspend fun unlockAvatar(emoji: String, priceGems: Int) = false
    override suspend fun exchangeGoldForGems() = false
    override suspend fun claimDailyCheckIn() = false
    override fun checkInThisWeek(): Flow<Set<Long>> = flowOf(emptySet())
  }

  private fun vm() = LevelsViewModel(levels, dailyAdventure, answerBus, controller, rewards)

  private fun statusOf(vm: LevelsViewModel, index: Int) =
    vm.state.value.nodes.first { it.def.index == index }.status

  @Test
  fun onlyTheFirstLevelStartsUnlocked() = runTest(dispatcher) {
    val vm = vm()
    advanceUntilIdle()
    assertEquals(LevelStatus.CURRENT, statusOf(vm, 1))
    assertEquals(LevelStatus.LOCKED, statusOf(vm, 2))
  }

  @Test
  fun answeringEnoughInARowCompletesTheLevelAndUnlocksTheNext() = runTest(dispatcher) {
    val vm = vm()
    advanceUntilIdle()
    val first = LEVELS.first()

    vm.onStart(first)
    repeat(first.answersToWin) {
      answerBus.report(true)
      advanceUntilIdle()
    }

    assertEquals(LevelStatus.DONE, statusOf(vm, 1))
    assertEquals(LevelStatus.CURRENT, statusOf(vm, 2))
    assertEquals(first.id, vm.justCompleted.value)
    assertEquals(first.id, controller.completed.value)
    assertNull(controller.active.value)
  }

  @Test
  fun aWrongAnswerResetsTheStreakSoAllMustBeRightInARow() = runTest(dispatcher) {
    val vm = vm()
    advanceUntilIdle()
    val first = LEVELS.first()
    assertEquals(3, first.answersToWin)

    vm.onStart(first)

    answerBus.report(true)
    answerBus.report(true)
    advanceUntilIdle()
    assertEquals(2, controller.streak.value)

    answerBus.report(false)
    advanceUntilIdle()
    assertEquals(0, controller.streak.value)
    assertEquals(LevelStatus.CURRENT, statusOf(vm, 1))

    answerBus.report(true)
    answerBus.report(true)
    advanceUntilIdle()
    assertEquals(LevelStatus.CURRENT, statusOf(vm, 1))

    answerBus.report(true)
    advanceUntilIdle()
    assertEquals(LevelStatus.DONE, statusOf(vm, 1))
    assertEquals(first.id, vm.justCompleted.value)
  }

  @Test
  fun aMemoryLevelCompletesByFinishingTheBoardNotByStreak() = runTest(dispatcher) {
    val vm = vm()
    advanceUntilIdle()
    val memory = LEVELS.first { it.kind == LevelKind.MEMORY }

    vm.onStart(memory)

    repeat(5) { answerBus.report(true) }
    advanceUntilIdle()
    assertNull(vm.justCompleted.value)
    assertNull(controller.completed.value)

    answerBus.reportFinished()
    advanceUntilIdle()
    assertEquals(LevelStatus.DONE, statusOf(vm, memory.index))
    assertEquals(memory.id, vm.justCompleted.value)
    assertEquals(memory.id, controller.completed.value)
  }

  @Test
  fun answersWithoutStartingALevelCompleteNothing() = runTest(dispatcher) {
    val vm = vm()
    advanceUntilIdle()

    repeat(10) { answerBus.report(true) }
    advanceUntilIdle()

    assertEquals(LevelStatus.CURRENT, statusOf(vm, 1))
    assertNull(vm.justCompleted.value)
  }

  @Test
  fun completingEveryLevelForTodayClaimsTheRewardExactlyOnce() = runTest(dispatcher) {
    val vm = vm()
    advanceUntilIdle()

    LEVELS.forEach { def ->
      vm.onStart(def)
      if (def.kind == LevelKind.MEMORY) {
        answerBus.reportFinished()
      } else {
        repeat(def.answersToWin) { answerBus.report(true) }
      }
      advanceUntilIdle()
    }

    assertTrue(vm.rewardEarned.value, "reward is claimed once every level for today is done")
    assertEquals(1, rewardClaims, "claimed exactly once, not once per level")
    assertTrue(gemsEarned > 0, "clearing the day should credit Gems")
  }

  @Test
  fun rewardIsNotClaimedWhileLevelsRemain() = runTest(dispatcher) {
    val vm = vm()
    advanceUntilIdle()
    val first = LEVELS.first()

    vm.onStart(first)
    repeat(first.answersToWin) { answerBus.report(true) }
    advanceUntilIdle()

    assertEquals(false, vm.rewardEarned.value)
    assertEquals(0, rewardClaims)
  }
}
