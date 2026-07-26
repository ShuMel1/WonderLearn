package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.RewardsRepository
import com.compose.wonderlearn.domain.STARTING_COINS
import com.compose.wonderlearn.domain.XP_PER_COIN
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
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : RewardsRepository {

  private val queries = database.wonderLearnQueries

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun <T> perActiveProfile(select: (String) -> Flow<T>): Flow<T> =
    profiles.activeProfileId().flatMapLatest { select(it) }

  private fun settingFlow(key: String): Flow<String?> =
    queries.selectSetting(key).asFlow().mapToOneOrNull(dispatcher)

  override fun coins(): Flow<Int> = combine(
    perActiveProfile { queries.selectTotalXp(it).asFlow().mapToOne(dispatcher) },
    perActiveProfile { settingFlow(spentKey(it)) },
  ) { totalXp, spentValue ->
    val earned = (totalXp / XP_PER_COIN).toInt() + STARTING_COINS
    val spent = spentValue?.toIntOrNull() ?: 0
    (earned - spent).coerceAtLeast(0)
  }

  override fun unlockedAvatars(): Flow<Set<String>> =
    perActiveProfile { settingFlow(unlockedKey(it)) }.map { it.parseIds() }

  override suspend fun unlockAvatar(emoji: String, price: Int): Boolean = withContext(dispatcher) {
    val profileId = profiles.currentProfileId()
    val unlocked = queries.selectSetting(unlockedKey(profileId)).executeAsOneOrNull().parseIds()
    if (emoji in unlocked) return@withContext true
    val totalXp = queries.selectTotalXp(profileId).executeAsOne()
    val spent = queries.selectSetting(spentKey(profileId)).executeAsOneOrNull()?.toIntOrNull() ?: 0
    val balance = (totalXp / XP_PER_COIN).toInt() + STARTING_COINS - spent
    if (balance < price) return@withContext false
    queries.transaction {
      queries.upsertSetting(spentKey(profileId), (spent + price).toString())
      queries.upsertSetting(unlockedKey(profileId), (unlocked + emoji).joinToString(SEPARATOR))
    }
    true
  }
}

private const val SEPARATOR = "|"

private fun String?.parseIds(): Set<String> =
  this?.split(SEPARATOR)?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

private fun spentKey(profileId: String) = "coins_spent:$profileId"
private fun unlockedKey(profileId: String) = "unlocked_avatars:$profileId"
