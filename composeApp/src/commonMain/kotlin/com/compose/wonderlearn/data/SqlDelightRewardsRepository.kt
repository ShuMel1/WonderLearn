package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.GEMS_PER_EXCHANGE
import com.compose.wonderlearn.domain.GOLD_PER_CHECKIN
import com.compose.wonderlearn.domain.GOLD_PER_EXCHANGE
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.RewardsRepository
import com.compose.wonderlearn.domain.STARTING_GOLD
import com.compose.wonderlearn.domain.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightRewardsRepository(
  database: WonderLearnDatabase,
  private val profiles: ProfileRepository,
  private val time: TimeProvider,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : RewardsRepository {

  private val queries = database.wonderLearnQueries

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun <T> perActiveProfile(select: (String) -> Flow<T>): Flow<T> =
    profiles.activeProfileId().flatMapLatest { select(it) }

  private fun settingFlow(key: String): Flow<String?> =
    queries.selectSetting(key).asFlow().mapToOneOrNull(dispatcher)

  private fun settingInt(key: String): Int =
    queries.selectSetting(key).executeAsOneOrNull()?.toIntOrNull() ?: 0

  override fun gold(): Flow<Int> = combine(
    perActiveProfile { settingFlow(goldEarnedKey(it)) },
    perActiveProfile { settingFlow(goldSpentKey(it)) },
  ) { earnedValue, spentValue ->
    val earned = earnedValue?.toIntOrNull() ?: 0
    val spent = spentValue?.toIntOrNull() ?: 0
    (STARTING_GOLD + earned - spent).coerceAtLeast(0)
  }

  override fun gems(): Flow<Int> = combine(
    perActiveProfile { settingFlow(gemsEarnedKey(it)) },
    perActiveProfile { settingFlow(gemsSpentKey(it)) },
  ) { earnedValue, spentValue ->
    val earned = earnedValue?.toIntOrNull() ?: 0
    val spent = spentValue?.toIntOrNull() ?: 0
    (earned - spent).coerceAtLeast(0)
  }

  override suspend fun earnGold(amount: Int) {
    withContext(dispatcher) {
      val profileId = profiles.currentProfileId()
      val key = goldEarnedKey(profileId)
      queries.upsertSetting(key, (settingInt(key) + amount).toString())
    }
  }

  override suspend fun earnGems(amount: Int) {
    withContext(dispatcher) {
      val profileId = profiles.currentProfileId()
      val key = gemsEarnedKey(profileId)
      queries.upsertSetting(key, (settingInt(key) + amount).toString())
    }
  }

  override fun unlockedAvatars(): Flow<Set<String>> =
    perActiveProfile { settingFlow(unlockedKey(it)) }.map { it.parseIds() }

  override suspend fun unlockAvatar(emoji: String, priceGems: Int): Boolean = withContext(dispatcher) {
    val profileId = profiles.currentProfileId()
    val unlocked = queries.selectSetting(unlockedKey(profileId)).executeAsOneOrNull().parseIds()
    if (emoji in unlocked) return@withContext true
    val earned = settingInt(gemsEarnedKey(profileId))
    val spent = settingInt(gemsSpentKey(profileId))
    val balance = (earned - spent).coerceAtLeast(0)
    if (balance < priceGems) return@withContext false
    queries.transaction {
      queries.upsertSetting(gemsSpentKey(profileId), (spent + priceGems).toString())
      queries.upsertSetting(unlockedKey(profileId), (unlocked + emoji).joinToString(SEPARATOR))
    }
    true
  }

  override suspend fun exchangeGoldForGems(): Boolean = withContext(dispatcher) {
    val profileId = profiles.currentProfileId()
    val goldEarned = settingInt(goldEarnedKey(profileId))
    val goldSpent = settingInt(goldSpentKey(profileId))
    val goldBalance = (STARTING_GOLD + goldEarned - goldSpent).coerceAtLeast(0)
    if (goldBalance < GOLD_PER_EXCHANGE) return@withContext false
    val gemsEarned = settingInt(gemsEarnedKey(profileId))
    queries.transaction {
      queries.upsertSetting(goldSpentKey(profileId), (goldSpent + GOLD_PER_EXCHANGE).toString())
      queries.upsertSetting(gemsEarnedKey(profileId), (gemsEarned + GEMS_PER_EXCHANGE).toString())
    }
    true
  }

  override suspend fun claimDailyCheckIn(): Boolean = withContext(dispatcher) {
    val profileId = profiles.currentProfileId()
    val day = time.todayEpochDay()
    val key = checkinKey(profileId, day)
    val alreadyClaimed = queries.selectSetting(key).executeAsOneOrNull() == "true"
    if (alreadyClaimed) return@withContext false
    val goldEarned = settingInt(goldEarnedKey(profileId))
    queries.transaction {
      queries.upsertSetting(key, "true")
      queries.upsertSetting(goldEarnedKey(profileId), (goldEarned + GOLD_PER_CHECKIN).toString())
    }
    true
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun checkInThisWeek(): Flow<Set<Long>> =
    profiles.activeProfileId().flatMapLatest { profileId ->
      val today = time.todayEpochDay()
      val days = (0..6).map { today - it }
      val dayFlows = days.map { day ->
        settingFlow(checkinKey(profileId, day)).map { day to (it == "true") }
      }
      combine(dayFlows) { pairs -> pairs.filter { it.second }.map { it.first }.toSet() }
    }
}

private const val SEPARATOR = "|"

private fun String?.parseIds(): Set<String> =
  this?.split(SEPARATOR)?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

private fun goldEarnedKey(profileId: String) = "gold_earned:$profileId"
private fun goldSpentKey(profileId: String) = "gold_spent:$profileId"
private fun gemsEarnedKey(profileId: String) = "gems_earned:$profileId"
private fun gemsSpentKey(profileId: String) = "gems_spent:$profileId"
private fun unlockedKey(profileId: String) = "unlocked_avatars:$profileId"
private fun checkinKey(profileId: String, day: Long) = "checkin_claimed:$profileId:$day"
