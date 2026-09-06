package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.DailyAdventureRepository
import com.compose.wonderlearn.domain.LevelDef
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.TimeProvider
import com.compose.wonderlearn.domain.dailyAdventureLevels
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightDailyAdventureRepository(
  database: WonderLearnDatabase,
  private val profiles: ProfileRepository,
  private val time: TimeProvider,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : DailyAdventureRepository {

  private val queries = database.wonderLearnQueries

  override fun todaysLevels(): List<LevelDef> = dailyAdventureLevels(time.todayEpochDay())

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun completedToday(): Flow<Set<String>> =
    profiles.activeProfileId().flatMapLatest { profileId ->
      queries.selectSetting(doneKey(profileId, time.todayEpochDay())).asFlow().mapToOneOrNull(dispatcher)
        .map { it.parseIds() }
    }

  override suspend fun markLevelDone(levelId: String) = withContext(dispatcher) {
    val profileId = profiles.currentProfileId()
    val key = doneKey(profileId, time.todayEpochDay())
    val done = queries.selectSetting(key).executeAsOneOrNull().parseIds()
    if (levelId in done) return@withContext
    queries.upsertSetting(key, (done + levelId).joinToString(SEPARATOR))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun rewardClaimed(): Flow<Boolean> =
    profiles.activeProfileId().flatMapLatest { profileId ->
      queries.selectSetting(claimKey(profileId, time.todayEpochDay())).asFlow().mapToOneOrNull(dispatcher)
        .map { it == "true" }
    }

  override suspend fun claimReward(): Boolean = withContext(dispatcher) {
    val profileId = profiles.currentProfileId()
    val day = time.todayEpochDay()
    val done = queries.selectSetting(doneKey(profileId, day)).executeAsOneOrNull().parseIds()
    val alreadyClaimed = queries.selectSetting(claimKey(profileId, day)).executeAsOneOrNull() == "true"
    val fullyDone = done.containsAll(dailyAdventureLevels(day).map { it.id })
    if (alreadyClaimed || !fullyDone) return@withContext false
    queries.upsertSetting(claimKey(profileId, day), "true")
    true
  }
}

private const val SEPARATOR = "|"

private fun String?.parseIds(): Set<String> =
  this?.split(SEPARATOR)?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

private fun doneKey(profileId: String, day: Long) = "adventure_done:$profileId:$day"
private fun claimKey(profileId: String, day: Long) = "adventure_reward_claimed:$profileId:$day"
