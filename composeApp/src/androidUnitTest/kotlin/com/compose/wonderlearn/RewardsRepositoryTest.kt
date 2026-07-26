package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.SqlDelightProfileRepository
import com.compose.wonderlearn.data.SqlDelightProgressRepository
import com.compose.wonderlearn.data.SqlDelightRewardsRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.RewardsRepository
import com.compose.wonderlearn.domain.STARTING_COINS
import com.compose.wonderlearn.domain.TimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RewardsRepositoryTest {

  private class Fixture(
    val progress: ProgressRepository,
    val rewards: RewardsRepository,
    val profiles: SqlDelightProfileRepository,
  )

  private fun newFixture(): Fixture {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val db = WonderLearnDatabase(driver)
    val dispatcher = UnconfinedTestDispatcher()
    val profiles = SqlDelightProfileRepository(db, dispatcher)
    val clock = object : TimeProvider { override fun todayEpochDay() = 500L }
    return Fixture(
      SqlDelightProgressRepository(db, profiles, clock, dispatcher),
      SqlDelightRewardsRepository(db, profiles, dispatcher),
      profiles,
    )
  }

  @Test
  fun coinsStartWithABonusAndTrackEarnedXp() = runTest {
    val f = newFixture()
    assertEquals(STARTING_COINS, f.rewards.coins().first(), "a new child starts with the welcome bonus")
    repeat(3) { f.progress.recordCorrectAnswer() } // 3 correct = 30 XP = 3 coins
    assertEquals(STARTING_COINS + 3, f.rewards.coins().first())
  }

  @Test
  fun unlockingAnAvatarSpendsCoinsAndOwnsIt() = runTest {
    val f = newFixture()
    repeat(5) { f.progress.recordCorrectAnswer() } // STARTING_COINS + 5

    assertTrue(f.rewards.unlockAvatar("🦁", 20))
    assertTrue("🦁" in f.rewards.unlockedAvatars().first())
    assertEquals(STARTING_COINS + 5 - 20, f.rewards.coins().first())
  }

  @Test
  fun cannotUnlockWhatYouCannotAfford() = runTest {
    val f = newFixture()
    assertFalse(f.rewards.unlockAvatar("🦖", 120))
    assertFalse("🦖" in f.rewards.unlockedAvatars().first())
    assertEquals(STARTING_COINS, f.rewards.coins().first(), "balance unchanged after a failed unlock")
  }

  @Test
  fun unlockingAnAvatarAlreadyOwnedIsANoOp() = runTest {
    val f = newFixture()
    f.rewards.unlockAvatar("🐸", 20)
    val after = f.rewards.coins().first()
    assertTrue(f.rewards.unlockAvatar("🐸", 20), "already unlocked counts as owned")
    assertEquals(after, f.rewards.coins().first(), "no coins spent unlocking it twice")
  }

  @Test
  fun anUnlockedAvatarStaysOwnedAfterSwitchingAway() = runTest {
    val f = newFixture()
    assertTrue(f.rewards.unlockAvatar("🦁", 20))
    val afterUnlock = f.rewards.coins().first()

    f.profiles.setAvatar(f.profiles.currentProfileId(), "🐱")
    f.profiles.setAvatar(f.profiles.currentProfileId(), "🦁")

    assertTrue("🦁" in f.rewards.unlockedAvatars().first(), "unlock survives switching avatars")
    assertEquals(afterUnlock, f.rewards.coins().first(), "wearing an owned avatar costs nothing")
  }
}
