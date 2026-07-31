package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.LevelsRepository
import com.compose.wonderlearn.domain.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightLevelsRepository(
  database: WonderLearnDatabase,
  private val profiles: ProfileRepository,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : LevelsRepository {

  private val queries = database.wonderLearnQueries

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun completedLevels(): Flow<Set<String>> =
    profiles.activeProfileId().flatMapLatest { profileId ->
      queries.selectSetting(completedKey(profileId)).asFlow().mapToOneOrNull(dispatcher)
        .map { it.parseIds() }
    }

  override suspend fun markComplete(levelId: String) = withContext(dispatcher) {
    val profileId = profiles.currentProfileId()
    val completed = queries.selectSetting(completedKey(profileId)).executeAsOneOrNull().parseIds()
    if (levelId in completed) return@withContext
    queries.upsertSetting(completedKey(profileId), (completed + levelId).joinToString(SEPARATOR))
  }
}

private const val SEPARATOR = "|"

private fun String?.parseIds(): Set<String> =
  this?.split(SEPARATOR)?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

private fun completedKey(profileId: String) = "levels_completed:$profileId"
