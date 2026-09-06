package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.SqlDelightDailyAdventureRepository
import com.compose.wonderlearn.data.SqlDelightProfileRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.DailyAdventureRepository
import com.compose.wonderlearn.domain.LEVELS
import com.compose.wonderlearn.domain.TimeProvider
import com.compose.wonderlearn.domain.dailyAdventureLevels
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyAdventureRepositoryTest {

  // ---- pure day -> level selection ----

  @Test fun theSameDayAlwaysPicksTheSameLevels() {
    val a = dailyAdventureLevels(day = 12345)
    val b = dailyAdventureLevels(day = 12345)
    assertEquals(a, b, "same day must be deterministic across calls, devices and platforms")
  }

  @Test fun theDefaultSliceIsTheWholeCurrentPoolSortedByIndex() {
    val levels = dailyAdventureLevels(day = 1)
    assertEquals(LEVELS.size, levels.size, "with only 12 templates today, every one of them plays each day")
    assertEquals(levels.sortedBy { it.index }, levels, "presented in ascending difficulty order")
    assertEquals(levels.map { it.id }.toSet().size, levels.size, "no duplicates")
  }

  @Test fun theUnderlyingShuffleStillVariesByDayOnceCountIsSmallerThanThePool() {
    // The default (whole pool) can't vary day to day — there's nothing left to choose between.
    // This exercises the day-seeded shuffle itself, which still matters the moment the pool grows
    // past what should be shown per day and an explicit smaller `count` is passed again.
    val days = (1L..30L).map { dailyAdventureLevels(day = it, count = 5).map { def -> def.id } }
    assertTrue(days.toSet().size > 1, "30 different days shouldn't all land on the exact same 5 levels")
  }

  @Test fun aPoolSmallerThanTheCountReturnsWhateverExists() {
    val small = LEVELS.take(2)
    assertEquals(2, dailyAdventureLevels(day = 1, pool = small, count = 5).size)
  }

  // ---- persistence + claim guarding ----

  private class FakeClock(var today: Long) : TimeProvider {
    override fun todayEpochDay(): Long = today
  }

  private fun newRepo(clock: TimeProvider): DailyAdventureRepository {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val db = WonderLearnDatabase(driver)
    val dispatcher = UnconfinedTestDispatcher()
    val profiles = SqlDelightProfileRepository(db, dispatcher)
    return SqlDelightDailyAdventureRepository(db, profiles, clock, dispatcher)
  }

  @Test fun markingALevelDoneIsReflectedInCompletedToday() = runTest {
    val repo = newRepo(FakeClock(today = 500))
    val levelId = repo.todaysLevels().first().id

    assertTrue(repo.completedToday().first().isEmpty())
    repo.markLevelDone(levelId)
    assertEquals(setOf(levelId), repo.completedToday().first())
  }

  @Test fun claimingBeforeEveryLevelIsDoneFails() = runTest {
    val repo = newRepo(FakeClock(today = 500))
    repo.markLevelDone(repo.todaysLevels().first().id)

    assertFalse(repo.claimReward(), "only one of five levels done")
    assertFalse(repo.rewardClaimed().first())
  }

  @Test fun claimingOnceEveryLevelIsDoneSucceedsExactlyOnce() = runTest {
    val repo = newRepo(FakeClock(today = 500))
    repo.todaysLevels().forEach { repo.markLevelDone(it.id) }

    assertTrue(repo.claimReward(), "every level for today is done")
    assertTrue(repo.rewardClaimed().first())
    assertFalse(repo.claimReward(), "already claimed today, must not pay out twice")
  }

  @Test fun aNewDayResetsCompletionAndAllowsClaimingAgain() = runTest {
    val clock = FakeClock(today = 500)
    val repo = newRepo(clock)
    repo.todaysLevels().forEach { repo.markLevelDone(it.id) }
    assertTrue(repo.claimReward())

    clock.today = 501

    assertTrue(repo.completedToday().first().isEmpty(), "a new day starts with nothing done")
    assertFalse(repo.rewardClaimed().first(), "a new day's reward hasn't been claimed yet")
    assertFalse(repo.claimReward(), "not done yet for the new day")
    repo.todaysLevels().forEach { repo.markLevelDone(it.id) }
    assertTrue(repo.claimReward(), "the new day can be claimed once it's done too")
  }
}
