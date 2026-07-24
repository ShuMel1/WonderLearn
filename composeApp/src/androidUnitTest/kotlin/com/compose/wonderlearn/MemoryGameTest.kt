package com.compose.wonderlearn

import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.domain.DailyProgress
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.feature.memory.MemoryGameViewModel
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
class MemoryGameTest {

  private val dispatcher = StandardTestDispatcher()

  @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private fun word(id: String) =
    VocabularyItem(id, "cat", "🍎", null, mapOf(Language.ENGLISH to id.uppercase()))

  private val vocabulary = object : VocabularyRepository {
    override fun categories(): Flow<List<Category>> = flowOf(emptyList())
    override fun itemsForCategory(categoryId: String): Flow<List<VocabularyItem>> = flowOf(emptyList())
    override suspend fun item(id: String): VocabularyItem? = null
    override suspend fun randomItems(count: Int): List<VocabularyItem> =
      (1..count).map { word("w$it") }
  }

  private val progress = object : ProgressRepository {
    override suspend fun recordCorrectAnswer() = Unit
    override suspend fun recordWordLearned() = Unit
    override fun dailyProgress(): Flow<DailyProgress> = flowOf(DailyProgress())
    override fun dailyGoal(): Flow<Int> = flowOf(5)
    override suspend fun setDailyGoal(goal: Int) = Unit
  }

  private fun game() = MemoryGameViewModel(vocabulary, progress)

  @Test
  fun newGameDealsEachWordAsExactlyTwoCards() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    val s = vm.state.value
    assertEquals(12, s.cards.size)
    assertEquals(6, s.totalPairs)
    assertTrue(s.cards.groupingBy { it.wordId }.eachCount().values.all { it == 2 })
    assertTrue(s.cards.none { it.revealed || it.matched }, "cards start face down")
  }

  @Test
  fun twoCardsOfTheSameWordMatch() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    val wordId = vm.state.value.cards.first().wordId
    val pair = vm.state.value.cards.filter { it.wordId == wordId }

    vm.onCardClick(pair[0].cardId)
    vm.onCardClick(pair[1].cardId)
    advanceUntilIdle()

    assertTrue(vm.state.value.cards.filter { it.wordId == wordId }.all { it.matched })
    assertEquals(1, vm.state.value.matchedPairs)
  }

  @Test
  fun twoDifferentCardsFlipBack() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    val cards = vm.state.value.cards
    val a = cards.first()
    val b = cards.first { it.wordId != a.wordId }

    vm.onCardClick(a.cardId)
    vm.onCardClick(b.cardId)
    advanceUntilIdle()

    val after = vm.state.value.cards
    assertFalse(after.first { it.cardId == a.cardId }.revealed, "mismatched cards flip back")
    assertFalse(after.first { it.cardId == b.cardId }.revealed)
    assertEquals(0, vm.state.value.matchedPairs)
  }

  @Test
  fun matchingEveryPairWinsTheGame() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    val byWord = vm.state.value.cards.groupBy { it.wordId }
    byWord.values.forEach { pair ->
      vm.onCardClick(pair[0].cardId)
      vm.onCardClick(pair[1].cardId)
      advanceUntilIdle()
    }
    assertTrue(vm.state.value.won)
    assertEquals(6, vm.state.value.matchedPairs)
  }
}
