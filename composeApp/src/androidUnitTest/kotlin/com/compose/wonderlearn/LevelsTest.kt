package com.compose.wonderlearn

import com.compose.wonderlearn.domain.AnswerBus
import com.compose.wonderlearn.domain.LEVELS
import com.compose.wonderlearn.domain.LevelKind
import com.compose.wonderlearn.domain.LevelRunController
import com.compose.wonderlearn.domain.LevelsRepository
import com.compose.wonderlearn.feature.levels.LevelStatus
import com.compose.wonderlearn.feature.levels.LevelsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class LevelsTest {

  private val dispatcher = StandardTestDispatcher()

  @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private val completed = MutableStateFlow<Set<String>>(emptySet())
  private val answerBus = AnswerBus()
  private val controller = LevelRunController()

  private val levels = object : LevelsRepository {
    override fun completedLevels(): Flow<Set<String>> = completed
    override suspend fun markComplete(levelId: String) { completed.value = completed.value + levelId }
  }

  private fun vm() = LevelsViewModel(levels, answerBus, controller)

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
}
