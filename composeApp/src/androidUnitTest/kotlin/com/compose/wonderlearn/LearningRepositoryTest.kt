package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.DatabaseSeeder
import com.compose.wonderlearn.data.SeedData
import com.compose.wonderlearn.data.SqlDelightLearningRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.QuizMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LearningRepositoryTest {

  private val hy = Language.ARMENIAN
  private val en = Language.ENGLISH

  private fun newRepository(dispatcher: CoroutineDispatcher): SqlDelightLearningRepository {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val database = WonderLearnDatabase(driver)
    DatabaseSeeder.seedIfEmpty(database)
    return SqlDelightLearningRepository(database, dispatcher)
  }

  @Test
  fun wordBecomesLearnedAfterThreeCorrectInARow() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    assertFalse(repo.recordCorrect("apple", hy), "streak 1 is not learned")
    assertFalse(repo.recordCorrect("apple", hy), "streak 2 is not learned")
    assertTrue(repo.recordCorrect("apple", hy), "streak 3 becomes learned")
    assertTrue(repo.learnedItems(hy).first().any { it.id == "apple" })
  }

  @Test
  fun wrongAnswerResetsTheStreak() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repo.recordCorrect("apple", hy)
    repo.recordCorrect("apple", hy)
    repo.recordWrong("apple", hy, QuizMode.LEARN)
    assertFalse(repo.recordCorrect("apple", hy), "after reset, one correct is streak 1")
    assertTrue(repo.learnedItems(hy).first().none { it.id == "apple" })
  }

  @Test
  fun quizNeverTargetsALearnedWord() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }
    repeat(60) {
      val round = repo.nextRound(hy, QuizMode.LEARN)
      assertNotNull(round)
      assertTrue(round.target.id != "apple", "learned word must not be a quiz target")
    }
  }

  @Test
  fun nextRoundIsNullWhenEverythingIsLearned() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    SeedData.words.forEach { word -> repeat(3) { repo.recordCorrect(word.id, hy) } }
    assertNull(repo.nextRound(hy, QuizMode.LEARN), "no rounds left once all words are learned")
  }

  @Test
  fun reviseQuizzesOnlyLearnedWords() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }
    repeat(3) { repo.recordCorrect("dog", hy) }

    repeat(40) {
      val round = repo.nextRound(hy, QuizMode.REVISE)
      assertNotNull(round)
      assertTrue(
        round.target.id == "apple" || round.target.id == "dog",
        "revise must only target learned words, got ${'$'}{round.target.id}",
      )
    }
  }

  @Test
  fun reviseIsEmptyUntilSomethingIsLearned() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    assertNull(repo.nextRound(hy, QuizMode.REVISE), "nothing learned yet, nothing to revise")
  }

  @Test
  fun aWrongReviseAnswerCostsOneStepNotTheWholeStreak() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }
    assertTrue(repo.learnedItems(hy).first().any { it.id == "apple" })

    repo.recordWrong("apple", hy, QuizMode.REVISE)
    assertTrue(
      repo.learnedItems(hy).first().none { it.id == "apple" },
      "3 -> 2 drops it out of learned",
    )

    assertTrue(
      repo.recordCorrect("apple", hy),
      "one correct answer takes 2 -> 3 and re-masters it",
    )
  }

  @Test
  fun aWrongLearnAnswerStillClearsTheWholeStreak() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }

    repo.recordWrong("apple", hy, QuizMode.LEARN)
    assertFalse(repo.recordCorrect("apple", hy), "streak was cleared, one correct is only 1")
    assertFalse(repo.recordCorrect("apple", hy), "still only 2")
    assertTrue(repo.recordCorrect("apple", hy), "3 again")
  }

  @Test
  fun progressInOneLanguageDoesNotLeakIntoAnother() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }

    assertTrue(repo.learnedItems(hy).first().any { it.id == "apple" }, "learned in Armenian")
    assertTrue(repo.learnedItems(en).first().none { it.id == "apple" }, "not learned in English")
  }

  @Test
  fun theSameWordIsStillQuizzedInALanguageWhereItIsNotLearned() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }

    var targetedInEnglish = false
    repeat(200) {
      val round = repo.nextRound(en, QuizMode.LEARN)
      assertNotNull(round)
      if (round.target.id == "apple") targetedInEnglish = true
    }
    assertTrue(targetedInEnglish, "a word learned in Armenian must still come up in English")
  }

  @Test
  fun everythingLearnedInOneLanguageLeavesOtherLanguagesUntouched() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    SeedData.words.forEach { word -> repeat(3) { repo.recordCorrect(word.id, hy) } }

    assertNull(repo.nextRound(hy, QuizMode.LEARN), "Armenian is complete")
    assertNotNull(repo.nextRound(en, QuizMode.LEARN), "English still has everything to learn")
  }
}
