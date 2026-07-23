package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

const val DEFAULT_PROFILE_ID = "local"

data class Profile(
  val id: String,
  val displayName: String,
  val avatarId: String?,
)

interface ProfileRepository {
  fun profiles(): Flow<List<Profile>>

  /** The profile all progress is recorded against, falling back to [DEFAULT_PROFILE_ID]. */
  fun activeProfileId(): Flow<String>

  suspend fun currentProfileId(): String

  suspend fun setActiveProfile(id: String)

  suspend fun createProfile(displayName: String, avatarId: String? = null): Profile

  suspend fun renameProfile(id: String, displayName: String)

  /**
   * Removes a profile and every progress row belonging to it. Refuses to delete the last
   * profile, and reassigns the active profile if the deleted one was active. Returns true if
   * the profile was deleted.
   */
  suspend fun deleteProfile(id: String): Boolean
}
