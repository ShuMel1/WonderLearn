package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Where every profile's language pair lived before it became per-profile — read as a fallback
 * for a profile that doesn't have its own key yet, and copied onto one the first time it's seen
 * (see [migrateLegacyFlatLanguageIfNeeded]) so the whole app isn't re-migrating on every read. */
private const val LEGACY_KEY_NATIVE_LANGUAGE = "nativeLanguage"
private const val LEGACY_KEY_TARGET_LANGUAGE = "language"

class SqlDelightLanguagePreferences(
  database: WonderLearnDatabase,
  private val profiles: ProfileRepository,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : LanguagePreferences {

  private val queries = database.wonderLearnQueries

  init {
    migrateLegacyFlatLanguageIfNeeded()
  }

  /**
   * One-time, synchronous, cheap (a handful of profiles at most for this app): if the old flat
   * keys have a value, copy it onto every profile that doesn't yet have its own per-profile key.
   * Leaves the legacy keys in place afterward — nothing reads them once this has run once.
   */
  private fun migrateLegacyFlatLanguageIfNeeded() {
    val legacyNative = queries.selectSetting(LEGACY_KEY_NATIVE_LANGUAGE).executeAsOneOrNull()
    val legacyTarget = queries.selectSetting(LEGACY_KEY_TARGET_LANGUAGE).executeAsOneOrNull()
    if (legacyNative == null && legacyTarget == null) return
    val profileIds = queries.selectAllProfiles().executeAsList().map { it.id }
    for (profileId in profileIds) {
      if (legacyNative != null && queries.selectSetting(nativeLanguageKey(profileId)).executeAsOneOrNull() == null) {
        queries.upsertSetting(nativeLanguageKey(profileId), legacyNative)
      }
      if (legacyTarget != null && queries.selectSetting(targetLanguageKey(profileId)).executeAsOneOrNull() == null) {
        queries.upsertSetting(targetLanguageKey(profileId), legacyTarget)
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun <T> perActiveProfile(select: (String) -> Flow<T>): Flow<T> =
    profiles.activeProfileId().flatMapLatest { select(it) }

  override fun nativeLanguage(): Flow<Language?> = perActiveProfile { languageFor(nativeLanguageKey(it)) }

  override fun targetLanguage(): Flow<Language?> = perActiveProfile { languageFor(targetLanguageKey(it)) }

  override suspend fun setNativeLanguage(language: Language) {
    withContext(dispatcher) {
      queries.upsertSetting(nativeLanguageKey(profiles.currentProfileId()), language.code)
    }
  }

  override suspend fun setTargetLanguage(language: Language) {
    withContext(dispatcher) {
      queries.upsertSetting(targetLanguageKey(profiles.currentProfileId()), language.code)
    }
  }

  private fun languageFor(key: String): Flow<Language?> =
    queries.selectSetting(key).asFlow().mapToOneOrNull(dispatcher)
      .map { code -> code?.let { c -> Language.entries.firstOrNull { it.code == c } } }
}

private fun nativeLanguageKey(profileId: String) = "nativeLanguage:$profileId"
private fun targetLanguageKey(profileId: String) = "language:$profileId"
