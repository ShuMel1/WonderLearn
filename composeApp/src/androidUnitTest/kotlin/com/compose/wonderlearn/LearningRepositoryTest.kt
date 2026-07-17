package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.DatabaseSeeder
import com.compose.wonderlearn.data.SeedData
import com.compose.wonderlearn.data.SqlDelightLearningRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LearningRepositoryTest {

  private fun newRepository(): SqlDelightLearningRepository {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val database = WonderLearnDatabase(driver)
    DatabaseSeeder.seedIfEmpty(database)
    return SqlDelightLearningRepository(database)
  }

  @Test
  fun wordBecomesLearnedAfterThreeCorrectInARow() = runTest {
    val repo = newRepository()
    assertFalse(repo.recordCorrect("apple"), "streak 1 is not learned")
    assertFalse(repo.recordCorrect("apple"), "streak 2 is not learned")
    assertTrue(repo.recordCorrect("apple"), "streak 3 becomes learned")
    assertTrue(repo.learnedItems().first().any { it.id == "apple" })
  }

  @Test
  fun wrongAnswerResetsTheStreak() = runTest {
    val repo = newRepository()
    repo.recordCorrect("apple")
    repo.recordCorrect("apple")
    repo.recordWrong("apple")
    assertFalse(repo.recordCorrect("apple"), "after reset, one correct is streak 1, not learned")
    assertTrue(repo.learnedItems().first().none { it.id == "apple" })
  }

  @Test
  fun quizNeverTargetsALearnedWord() = runTest {
    val repo = newRepository()
    repeat(3) { repo.recordCorrect("apple") }
    repeat(60) {
      val round = repo.nextRound()
      assertNotNull(round)
      assertTrue(round.target.id != "apple", "learned word must not be a quiz target")
    }
  }

  @Test
  fun nextRoundIsNullWhenEverythingIsLearned() = runTest {
    val repo = newRepository()
    SeedData.words.forEach { word -> repeat(3) { repo.recordCorrect(word.id) } }
    assertNull(repo.nextRound(), "no rounds left once all words are learned")
  }
}
