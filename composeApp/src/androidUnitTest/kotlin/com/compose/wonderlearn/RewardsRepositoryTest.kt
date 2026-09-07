package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.SqlDelightProfileRepository
import com.compose.wonderlearn.data.SqlDelightRewardsRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.CHECKIN_JACKPOT_GOLD
import com.compose.wonderlearn.domain.CHECKIN_LADDER_SIZE
import com.compose.wonderlearn.domain.GEMS_PER_EXCHANGE
import com.compose.wonderlearn.domain.GOLD_PER_EXCHANGE
import com.compose.wonderlearn.domain.RewardsRepository
import com.compose.wonderlearn.domain.STARTING_GOLD
import com.compose.wonderlearn.domain.TimeProvider
import com.compose.wonderlearn.domain.checkInLadderPosition
import com.compose.wonderlearn.domain.checkInRewardForPosition
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

  private class FakeClock(var today: Long) : TimeProvider {
    override fun todayEpochDay() = today
  }

  private fun newFixture(today: Long = 500): Fixture = newFixture(FakeClock(today))

  private fun newFixture(clock: FakeClock): Fixture {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val db = WonderLearnDatabase(driver)
    val dispatcher = UnconfinedTestDispatcher()
    val profiles = SqlDelightProfileRepository(db, dispatcher)
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

  // ---- Daily check-in — pure ladder rules ----

  @Test
  fun ladderPositionStartsAtOneWithNoHistory() {
    assertEquals(1, checkInLadderPosition(day = 500, claimedDaysBefore = emptySet()))
  }

  @Test
  fun ladderPositionClimbsWithAnUnbrokenRun() {
    // Days 497, 498, 499 claimed, consecutively ending the day before day 500.
    val claimed = setOf(497L, 498L, 499L)
    assertEquals(4, checkInLadderPosition(day = 500, claimedDaysBefore = claimed))
  }

  @Test
  fun ladderPositionWrapsAfterAFullWeek() {
    val sixDaysBack = (494L..499L).toSet()
    assertEquals(CHECKIN_LADDER_SIZE, checkInLadderPosition(day = 500, claimedDaysBefore = sixDaysBack))
    val sevenDaysBack = (493L..499L).toSet()
    assertEquals(1, checkInLadderPosition(day = 500, claimedDaysBefore = sevenDaysBack), "a full week wraps back to slot 1")
  }

  @Test
  fun ladderPositionResetsOnAGap() {
    // 498 claimed, but 499 (yesterday) wasn't — the run is broken regardless of older history.
    val claimed = setOf(495L, 496L, 497L, 498L)
    assertEquals(1, checkInLadderPosition(day = 500, claimedDaysBefore = claimed))
  }

  @Test
  fun rewardClimbsByOneThenJackpotsOnTheLastSlot() {
    assertEquals(2, checkInRewardForPosition(1))
    assertEquals(3, checkInRewardForPosition(2))
    assertEquals(7, checkInRewardForPosition(CHECKIN_LADDER_SIZE - 1))
    assertEquals(CHECKIN_JACKPOT_GOLD, checkInRewardForPosition(CHECKIN_LADDER_SIZE))
  }

  // ---- Daily check-in — repository behavior ----

  @Test
  fun checkInPaysOutOnceADay() = runTest {
    val f = newFixture(today = 500)
    assertTrue(f.rewards.claimDailyCheckIn())
    assertEquals(STARTING_GOLD + 2, f.rewards.gold().first(), "day 1 of a fresh ladder pays 2")
    assertFalse(f.rewards.claimDailyCheckIn(), "already claimed today")
    assertEquals(STARTING_GOLD + 2, f.rewards.gold().first(), "a repeat claim must not pay out again")
  }

  @Test
  fun ladderPositionIsStableBeforeAndAfterClaiming() = runTest {
    val f = newFixture(today = 500)
    val before = f.rewards.checkInLadderPosition().first()
    f.rewards.claimDailyCheckIn()
    val after = f.rewards.checkInLadderPosition().first()
    assertEquals(before, after, "claiming today shouldn't change what slot today itself reads as")
  }

  @Test
  fun sevenConsecutiveDaysClimbTheLadderToTheJackpotThenWrap() = runTest {
    val clock = FakeClock(today = 500)
    val f = newFixture(clock)
    val expectedRewards = listOf(2, 3, 4, 5, 6, 7, CHECKIN_JACKPOT_GOLD, 2)
    var expectedGold = STARTING_GOLD
    for (reward in expectedRewards) {
      assertTrue(f.rewards.claimDailyCheckIn(), "day ${clock.today} should be claimable")
      expectedGold += reward
      assertEquals(expectedGold, f.rewards.gold().first(), "day ${clock.today}'s payout")
      clock.today += 1
    }
  }

  @Test
  fun missingADayResetsTheLadderToTheStart() = runTest {
    val clock = FakeClock(today = 500)
    val f = newFixture(clock)
    f.rewards.claimDailyCheckIn() // day 500: slot 1, +2
    clock.today += 1
    f.rewards.claimDailyCheckIn() // day 501: slot 2, +3
    clock.today += 2 // day 502 skipped entirely — the streak breaks
    assertTrue(f.rewards.claimDailyCheckIn())
    assertEquals(STARTING_GOLD + 2 + 3 + 2, f.rewards.gold().first(), "back to slot 1 after the missed day")
  }
}
