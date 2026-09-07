package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.SqlDelightProfileRepository
import com.compose.wonderlearn.data.SqlDelightRewardsRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.GEMS_PER_EXCHANGE
import com.compose.wonderlearn.domain.GOLD_PER_CHECKIN
import com.compose.wonderlearn.domain.GOLD_PER_EXCHANGE
import com.compose.wonderlearn.domain.RewardsRepository
import com.compose.wonderlearn.domain.STARTING_GOLD
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
    val rewards: RewardsRepository,
    val profiles: SqlDelightProfileRepository,
  )

  private fun newFixture(today: Long = 500): Fixture {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val db = WonderLearnDatabase(driver)
    val dispatcher = UnconfinedTestDispatcher()
    val profiles = SqlDelightProfileRepository(db, dispatcher)
    val clock = object : TimeProvider { override fun todayEpochDay() = today }
    return Fixture(
      SqlDelightRewardsRepository(db, profiles, clock, dispatcher),
      profiles,
    )
  }

  // ---- Gold ----

  @Test
  fun goldStartsWithAWelcomeBonusAndTracksEarning() = runTest {
    val f = newFixture()
    assertEquals(STARTING_GOLD, f.rewards.gold().first(), "a new child starts with the welcome bonus")
    f.rewards.earnGold(2)
    f.rewards.earnGold(3)
    assertEquals(STARTING_GOLD + 5, f.rewards.gold().first())
  }

  // ---- Gems ----

  @Test
  fun gemsStartAtZeroAndOnlyComeFromEarning() = runTest {
    val f = newFixture()
    assertEquals(0, f.rewards.gems().first(), "unlike Gold, Gems have no starting bonus")
    f.rewards.earnGems(3)
    assertEquals(3, f.rewards.gems().first())
  }

  // ---- Avatars, priced in Gems ----

  @Test
  fun unlockingAnAvatarSpendsGemsAndOwnsIt() = runTest {
    val f = newFixture()
    f.rewards.earnGems(10)

    assertTrue(f.rewards.unlockAvatar("🦁", 5))
    assertTrue("🦁" in f.rewards.unlockedAvatars().first())
    assertEquals(5, f.rewards.gems().first())
  }

  @Test
  fun cannotUnlockWhatYouCannotAfford() = runTest {
    val f = newFixture()
    assertFalse(f.rewards.unlockAvatar("🦖", 30))
    assertFalse("🦖" in f.rewards.unlockedAvatars().first())
    assertEquals(0, f.rewards.gems().first(), "balance unchanged after a failed unlock")
  }

  @Test
  fun unlockingAnAvatarAlreadyOwnedIsANoOp() = runTest {
    val f = newFixture()
    f.rewards.earnGems(10)
    f.rewards.unlockAvatar("🐸", 5)
    val after = f.rewards.gems().first()
    assertTrue(f.rewards.unlockAvatar("🐸", 5), "already unlocked counts as owned")
    assertEquals(after, f.rewards.gems().first(), "no Gems spent unlocking it twice")
  }

  @Test
  fun anUnlockedAvatarStaysOwnedAfterSwitchingAway() = runTest {
    val f = newFixture()
    f.rewards.earnGems(10)
    assertTrue(f.rewards.unlockAvatar("🦁", 5))
    val afterUnlock = f.rewards.gems().first()

    f.profiles.setAvatar(f.profiles.currentProfileId(), "🐱")
    f.profiles.setAvatar(f.profiles.currentProfileId(), "🦁")

    assertTrue("🦁" in f.rewards.unlockedAvatars().first(), "unlock survives switching avatars")
    assertEquals(afterUnlock, f.rewards.gems().first(), "wearing an owned avatar costs nothing")
  }

  // ---- Gold -> Gems exchange ----

  @Test
  fun exchangingGoldForGemsMovesBothBalances() = runTest {
    val f = newFixture()
    assertTrue(f.rewards.exchangeGoldForGems())
    assertEquals(STARTING_GOLD - GOLD_PER_EXCHANGE, f.rewards.gold().first())
    assertEquals(GEMS_PER_EXCHANGE, f.rewards.gems().first())
  }

  @Test
  fun exchangeFailsWithoutEnoughGold() = runTest {
    val f = newFixture()
    repeat(2) { f.rewards.exchangeGoldForGems() } // STARTING_GOLD=20 covers exactly 2 exchanges of 10
    assertFalse(f.rewards.exchangeGoldForGems(), "only 0 Gold left, can't afford a third exchange")
    assertEquals(0, f.rewards.gold().first())
    assertEquals(2, f.rewards.gems().first(), "the failed exchange must not have paid out anyway")
  }

  // ---- Daily check-in ----

  @Test
  fun checkInPaysOutOnceADayAndTracksTheWeek() = runTest {
    val f = newFixture(today = 500)
    assertTrue(f.rewards.claimDailyCheckIn())
    assertEquals(STARTING_GOLD + GOLD_PER_CHECKIN, f.rewards.gold().first())
    assertFalse(f.rewards.claimDailyCheckIn(), "already claimed today")
    assertEquals(STARTING_GOLD + GOLD_PER_CHECKIN, f.rewards.gold().first(), "a repeat claim must not pay out again")
    assertEquals(setOf(500L), f.rewards.checkInThisWeek().first())
  }

  @Test
  fun checkInStripOnlyCountsClaimedDays() = runTest {
    val f = newFixture(today = 500)
    assertTrue(f.rewards.checkInThisWeek().first().isEmpty(), "nothing claimed yet")
    f.rewards.claimDailyCheckIn()
    assertEquals(setOf(500L), f.rewards.checkInThisWeek().first())
  }
}
