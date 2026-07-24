package com.compose.wonderlearn.data.content

import com.compose.wonderlearn.shared.ContentManifest
import com.compose.wonderlearn.data.ioDispatcher
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.DEFAULT_PROFILE_ID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val KEY_CONTENT_VERSION = "contentVersion"
private const val DEFAULT_PROFILE_NAME = "Me"

/**
 * Applies a content manifest to the database, skipping work when the stored version is already
 * current. Words and categories are replaced; progress is keyed separately and left untouched.
 */
class ContentSeeder(
  private val database: WonderLearnDatabase,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) {

  private val queries = database.wonderLearnQueries

  // Serialises sync so two callers (e.g. startup and a manual/periodic refresh) can't both pass
  // the version check and apply the same manifest twice — the check and the apply act as one.
  private val syncMutex = Mutex()

  /** True once any content exists locally, meaning startup need not wait on the network. */
  suspend fun hasContent(): Boolean = withContext(dispatcher) {
    queries.countCategories().executeAsOne() > 0L
  }

  suspend fun sync(source: ContentSource): Boolean = syncMutex.withLock {
    withContext(dispatcher) {
      queries.ensureDefaultProfile(DEFAULT_PROFILE_ID, DEFAULT_PROFILE_NAME)
      val manifest = source.load() ?: return@withContext false
      if (manifest.version <= currentVersion()) return@withContext false
      apply(manifest)
      true
    }
  }

  private fun currentVersion(): Long =
    queries.selectSetting(KEY_CONTENT_VERSION).executeAsOneOrNull()?.toLongOrNull() ?: 0L

  fun apply(manifest: ContentManifest) {
    queries.transaction {
      manifest.categories.forEach {
        queries.upsertCategory(it.id, it.title, it.emoji, it.sortIndex, it.imageRef)
      }
      manifest.words.forEach { word ->
        queries.upsertWord(word.id, word.categoryId, word.emoji, word.imageRef)
        word.translations.forEach { (languageCode, text) ->
          queries.upsertTranslation(word.id, languageCode, text)
        }
      }
      queries.upsertSetting(KEY_CONTENT_VERSION, manifest.version.toString())
    }
  }

  suspend fun storedVersion(): Long = withContext(dispatcher) {
    queries.selectSetting(KEY_CONTENT_VERSION).executeAsOneOrNull()?.toLongOrNull() ?: 0L
  }
}
