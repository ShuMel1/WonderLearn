package com.compose.wonderlearn

import com.compose.wonderlearn.domain.AnswerBus
import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.domain.DailyProgress
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.LevelRunController
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.RewardsRepository
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.feature.bubblepop.Bubble
import com.compose.wonderlearn.feature.bubblepop.BubblePopViewModel
import com.compose.wonderlearn.feature.bubblepop.COIN_STREAK_GOAL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BubblePopTest {

  private val dispatcher = StandardTestDispatcher()

  @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private fun word(id: String) =
    VocabularyItem(id, "animals", "🐰", null, mapOf(Language.ENGLISH to id))

  private val pool = (1..8).map { word("w$it") }

  private val vocabulary = object : VocabularyRepository {
    override fun categories(): Flow<List<Category>> = flowOf(emptyList())
    override fun itemsForCategory(categoryId: String): Flow<List<VocabularyItem>> = flowOf(emptyList())
    override suspend fun item(id: String): VocabularyItem? = null
    override suspend fun randomItems(count: Int): List<VocabularyItem> = pool
  }

  private val progress = object : ProgressRepository {
    override suspend fun recordCorrectAnswer() = Unit
    override suspend fun recordWordLearned() = Unit
    override fun dailyProgress(): Flow<DailyProgress> = flowOf(DailyProgress())
    override fun dailyGoal(): Flow<Int> = flowOf(5)
    override suspend fun setDailyGoal(goal: Int) = Unit
  }

  private val pronouncer = object : Pronouncer {
    override suspend fun pronounce(item: VocabularyItem, language: Language) = true
    override fun stop() = Unit
  }

  private val preferences = object : LanguagePreferences {
    override fun nativeLanguage(): Flow<Language?> = flowOf(Language.ENGLISH)
    override fun targetLanguage(): Flow<Language?> = flowOf(Language.ENGLISH)
    override suspend fun setNativeLanguage(language: Language) = Unit
    override suspend fun setTargetLanguage(language: Language) = Unit
  }

  private var goldEarned = 0
  private val rewards = object : RewardsRepository {
    override fun gold(): Flow<Int> = flowOf(0)
    override fun gems(): Flow<Int> = flowOf(0)
    override suspend fun earnGold(amount: Int) { goldEarned += amount }
    override suspend fun earnGems(amount: Int) = Unit
    override fun unlockedAvatars(): Flow<Set<String>> = flowOf(emptySet())
    override suspend fun unlockAvatar(emoji: String, priceGems: Int) = false
    override suspend fun exchangeGoldForGems() = false
    override suspend fun claimDailyCheckIn() = false
    override fun checkedInToday(): Flow<Boolean> = flowOf(false)
    override fun checkInLadderPosition(): Flow<Int> = flowOf(1)
  }

  private fun game(fromLevel: Boolean = false, runController: LevelRunController = LevelRunController()) =
    BubblePopViewModel(vocabulary, progress, pronouncer, preferences, AnswerBus(), rewards, runController, fromLevel)

  private fun correctBubble(vm: BubblePopViewModel): Bubble =
    vm.state.value.bubbles.first { it.item.id == vm.state.value.targetId }

  private fun wrongBubble(vm: BubblePopViewModel): Bubble =
    vm.state.value.bubbles.first { it.item.id != vm.state.value.targetId }

  @Test
  fun consecutiveCorrectAnswersIncrementTheStreak() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    repeat(3) {
      vm.onPop(correctBubble(vm))
      advanceUntilIdle()
    }
    assertEquals(3, vm.state.value.streak)
    assertFalse(vm.state.value.rewardPending)
  }

  @Test
  fun aWrongTapFullyResetsTheStreak() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    repeat(2) {
      vm.onPop(correctBubble(vm))
      advanceUntilIdle()
    }
    assertEquals(2, vm.state.value.streak)
    vm.onPop(wrongBubble(vm))
    assertEquals(0, vm.state.value.streak)
  }

  @Test
  fun escapingFullyResetsTheStreak() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    repeat(2) {
      vm.onPop(correctBubble(vm))
      advanceUntilIdle()
    }
    assertEquals(2, vm.state.value.streak)
    vm.onEscaped()
    assertEquals(0, vm.state.value.streak)
  }

  @Test
  fun reachingTheStreakGoalPausesForTheRewardInsteadOfStartingANewRound() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    val roundKeyBeforeGoal = run {
      repeat(COIN_STREAK_GOAL - 1) {
        vm.onPop(correctBubble(vm))
        advanceUntilIdle()
      }
      vm.state.value.roundKey
    }
    vm.onPop(correctBubble(vm))
    assertEquals(COIN_STREAK_GOAL, vm.state.value.streak)
    assertTrue(vm.state.value.rewardPending)
    // No new round was dealt while the reward is pending.
    assertEquals(roundKeyBeforeGoal, vm.state.value.roundKey)
  }

  @Test
  fun claimingTheRewardClearsStreakAndResumesWithANewRound() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    repeat(COIN_STREAK_GOAL) {
      vm.onPop(correctBubble(vm))
      advanceUntilIdle()
    }
    val roundKeyAtReward = vm.state.value.roundKey
    val claimed = vm.claimStreakReward()
    advanceUntilIdle()
    assertTrue(claimed)
    assertEquals(0, vm.state.value.streak)
    assertFalse(vm.state.value.rewardPending)
    assertTrue(vm.state.value.roundKey > roundKeyAtReward, "a fresh round should have started")
    assertTrue(goldEarned > 0, "claiming the streak reward should credit Gold")
  }

  @Test
  fun theStreakNeverTracksWhenPlayedFromALevel() = runTest(dispatcher) {
    val vm = game(fromLevel = true)
    advanceUntilIdle()
    repeat(COIN_STREAK_GOAL) {
      vm.onPop(correctBubble(vm))
      advanceUntilIdle()
    }
    assertEquals(0, vm.state.value.streak)
    assertFalse(vm.state.value.rewardPending)
  }

  @Test
  fun theLastCorrectPopThatFinishesALevelDoesNotStartAnotherRound() = runTest(dispatcher) {
    // Regression test: BubblePop used to unconditionally call newRound() (and pronounce its new
    // target word) on every correct pop, including the one that satisfies the level's goal — the
    // level then popped the screen before the child ever saw that extra round, but the word still
    // got pronounced. newRound() must not run once this pop reaches the level's goal.
    val runController = LevelRunController()
    runController.begin("level-1", goal = 2)
    val vm = game(fromLevel = true, runController = runController)
    advanceUntilIdle()

    vm.onPop(correctBubble(vm))
    advanceUntilIdle()
    val roundKeyAfterFirst = vm.state.value.roundKey

    vm.onPop(correctBubble(vm))
    advanceUntilIdle()

    assertEquals(roundKeyAfterFirst, vm.state.value.roundKey, "no new round once the level's goal is reached")
  }

  @Test
  fun toggleSpeedFlipsFastMode() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    assertFalse(vm.state.value.fastMode)
    vm.toggleSpeed()
    assertTrue(vm.state.value.fastMode)
    vm.toggleSpeed()
    assertFalse(vm.state.value.fastMode)
  }
}
