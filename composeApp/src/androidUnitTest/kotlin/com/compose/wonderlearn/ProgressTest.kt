package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.SqlDelightProfileRepository
import com.compose.wonderlearn.data.SqlDelightProgressRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.DEFAULT_DAILY_GOAL
import com.compose.wonderlearn.domain.TimeProvider
import com.compose.wonderlearn.domain.computeStreak
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgressTest {

  private class FakeClock(var today: Long) : TimeProvider {
    override fun todayEpochDay(): Long = today
  }

  private fun newRepo(clock: TimeProvider) = run {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val db = WonderLearnDatabase(driver)
    val dispatcher = UnconfinedTestDispatcher()
    val profiles = SqlDelightProfileRepository(db, dispatcher)
    SqlDelightProgressRepository(db, profiles, clock, dispatcher)
  }

  // ---- pure streak logic ----

  @Test fun noActivityIsNoStreak() {
    assertEquals(0, computeStreak(today = 100, activeDaysDescending = emptyList()))
  }

  @Test fun todayOnlyIsStreakOfOne() {
    assertEquals(1, computeStreak(today = 100, activeDaysDescending = listOf(100)))
  }

  @Test fun consecutiveDaysEndingTodayCount() {
    assertEquals(3, computeStreak(today = 100, activeDaysDescending = listOf(100, 99, 98)))
  }

  @Test fun aGapBreaksTheStreak() {
    assertEquals(2, computeStreak(today = 100, activeDaysDescending = listOf(100, 99, 97, 96)))
  }

  @Test fun yesterdayButNotTodayStillCounts() {
    // the streak holds through today until the day ends
    assertEquals(2, computeStreak(today = 100, activeDaysDescending = listOf(99, 98)))
  }

  @Test fun olderThanYesterdayIsBroken() {
    assertEquals(0, computeStreak(today = 100, activeDaysDescending = listOf(98, 97)))
  }

  // ---- recording ----

  @Test fun correctAnswerAddsXpButNotAWord() = runTest {
    val repo = newRepo(FakeClock(today = 500))
    repo.recordCorrectAnswer()
    val p = repo.dailyProgress().first()
    assertEquals(0, p.wordsToday)
    assertTrue(p.totalXp > 0)
    assertFalse(p.goalReached)
  }

  @Test fun learningWordsCountsTowardTheDailyGoal() = runTest {
    val repo = newRepo(FakeClock(today = 500))
    repeat(DEFAULT_DAILY_GOAL) { repo.recordWordLearned() }
    val p = repo.dailyProgress().first()
    assertEquals(DEFAULT_DAILY_GOAL, p.wordsToday)
    assertTrue(p.goalReached, "hitting the goal count marks the goal reached")
    assertEquals(1, p.streakDays, "a day with words learned is a one-day streak")
  }

  @Test fun theDailyGoalIsConfigurableAndReflectedInProgress() = runTest {
    val repo = newRepo(FakeClock(today = 500))
    assertEquals(com.compose.wonderlearn.domain.DEFAULT_DAILY_GOAL, repo.dailyProgress().first().dailyGoal)
    repo.setDailyGoal(10)
    val p = repo.dailyProgress().first()
    assertEquals(10, p.dailyGoal)
    assertEquals(10, repo.dailyGoal().first())
    assertFalse(p.goalReached, "five words no longer reaches a goal of ten")
  }

  @Test fun activityOnConsecutiveDaysBuildsAStreak() = runTest {
    val clock = FakeClock(today = 500)
    val repo = newRepo(clock)
    repo.recordWordLearned()
    clock.today = 501
    repo.recordWordLearned()
    clock.today = 502
    repo.recordWordLearned()
    assertEquals(3, repo.dailyProgress().first().streakDays)
  }
}
