package com.compose.wonderlearn

import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.domain.DailyProgress
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.feature.oddoneout.OddOneOutViewModel
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OddOneOutTest {

  private val dispatcher = StandardTestDispatcher()

  @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private fun word(id: String, category: String) =
    VocabularyItem(id, category, "🍎", null, mapOf(Language.ENGLISH to id))

  private val pool = (1..5).map { word("a$it", "fruits") } + (1..5).map { word("b$it", "animals") }

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

  private fun game() = OddOneOutViewModel(vocabulary, progress)

  @Test
  fun aRoundHasFourOptionsWithExactlyOneOddCategory() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    val s = vm.state.value
    assertEquals(4, s.options.size)
    assertNotNull(s.oddId)
    val odd = s.options.first { it.id == s.oddId }
    val others = s.options.filter { it.id != s.oddId }
    assertTrue(others.all { it.categoryId == others.first().categoryId }, "the three belong together")
    assertTrue(others.none { it.categoryId == odd.categoryId }, "the odd one is a different category")
  }

  @Test
  fun tappingTheOddOneScoresAndSolves() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    val odd = vm.state.value.options.first { it.id == vm.state.value.oddId }
    vm.onSelect(odd)
    assertTrue(vm.state.value.solved)
    assertEquals(1, vm.state.value.score)
  }

  @Test
  fun tappingAGroupItemMarksItWrongWithoutSolving() = runTest(dispatcher) {
    val vm = game()
    advanceUntilIdle()
    val notOdd = vm.state.value.options.first { it.id != vm.state.value.oddId }
    vm.onSelect(notOdd)
    assertFalse(vm.state.value.solved)
    assertEquals(notOdd.id, vm.state.value.wrongId)
    assertEquals(0, vm.state.value.score)
  }
}
