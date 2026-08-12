package com.compose.wonderlearn.data.analytics

import com.compose.wonderlearn.data.ioDispatcher
import com.compose.wonderlearn.db.WonderLearnDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val KEY_INSTALL_ID = "analyticsInstallId"
private const val KEY_INSTALL_REPORTED = "analyticsInstallReported"

/**
 * A random id generated once per install so usage events can be grouped without identifying the
 * child. It is never tied to a profile, name or device identifier, and lives only in the local db.
 */
class InstallId(
  database: WonderLearnDatabase,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) {
  private val queries = database.wonderLearnQueries
  private val mutex = Mutex()
  private var cached: String? = null

  @OptIn(ExperimentalUuidApi::class)
  suspend fun value(): String = mutex.withLock {
    cached ?: withContext(dispatcher) {
      val id = queries.selectSetting(KEY_INSTALL_ID).executeAsOneOrNull()
        ?: Uuid.random().toString().also { queries.upsertSetting(KEY_INSTALL_ID, it) }
      id.also { cached = it }
    }
  }

  /** True exactly once per install, so an install event is reported a single time. */
  suspend fun firstReport(): Boolean = mutex.withLock {
    withContext(dispatcher) {
      if (queries.selectSetting(KEY_INSTALL_REPORTED).executeAsOneOrNull() != null) {
        false
      } else {
        queries.upsertSetting(KEY_INSTALL_REPORTED, "1")
        true
      }
    }
  }
}
