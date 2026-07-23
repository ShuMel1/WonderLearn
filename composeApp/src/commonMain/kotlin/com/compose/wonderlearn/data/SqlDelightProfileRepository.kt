package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.compose.wonderlearn.db.Profile as ProfileEntity
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.DEFAULT_PROFILE_ID
import com.compose.wonderlearn.domain.Profile
import com.compose.wonderlearn.domain.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.random.Random

private const val KEY_ACTIVE_PROFILE = "activeProfileId"

class SqlDelightProfileRepository(
  database: WonderLearnDatabase,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : ProfileRepository {

  private val queries = database.wonderLearnQueries

  override fun profiles(): Flow<List<Profile>> =
    queries.selectAllProfiles().asFlow().mapToList(dispatcher)
      .map { rows -> rows.map { it.toDomain() } }

  override fun activeProfileId(): Flow<String> =
    queries.selectSetting(KEY_ACTIVE_PROFILE).asFlow().mapToOneOrNull(dispatcher)
      .map { it ?: DEFAULT_PROFILE_ID }

  override suspend fun currentProfileId(): String =
    withContext(dispatcher) {
      queries.selectSetting(KEY_ACTIVE_PROFILE).executeAsOneOrNull() ?: DEFAULT_PROFILE_ID
    }

  override suspend fun setActiveProfile(id: String) {
    withContext(dispatcher) {
      queries.upsertSetting(KEY_ACTIVE_PROFILE, id)
    }
  }

  override suspend fun createProfile(displayName: String, avatarId: String?): Profile =
    withContext(dispatcher) {
      val profile = Profile(id = newProfileId(), displayName = displayName, avatarId = avatarId)
      queries.transaction {
        val sortIndex = queries.countProfiles().executeAsOne()
        queries.insertProfile(profile.id, profile.displayName, profile.avatarId, sortIndex)
      }
      profile
    }

  override suspend fun renameProfile(id: String, displayName: String) {
    val name = displayName.trim()
    if (name.isEmpty()) return
    withContext(dispatcher) { queries.renameProfile(name, id) }
  }

  override suspend fun deleteProfile(id: String): Boolean =
    withContext(dispatcher) {
      queries.transactionWithResult {
        if (queries.countProfiles().executeAsOne() <= 1L) return@transactionWithResult false
        if (currentActiveProfileId() == id) {
          val next = queries.selectAnyOtherProfileId(id).executeAsOneOrNull()
          if (next != null) queries.upsertSetting(KEY_ACTIVE_PROFILE, next)
        }
        queries.deleteProfileProgress(id)
        queries.deleteProfile(id)
        true
      }
    }

  private fun currentActiveProfileId(): String =
    queries.selectSetting(KEY_ACTIVE_PROFILE).executeAsOneOrNull() ?: DEFAULT_PROFILE_ID
}

private fun newProfileId(): String =
  "p-" + Random.nextLong(1L shl 62).toString(16)

private fun ProfileEntity.toDomain() =
  Profile(id = id, displayName = displayName, avatarId = avatarId)
