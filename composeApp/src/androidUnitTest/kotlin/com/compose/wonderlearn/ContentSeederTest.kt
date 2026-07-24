package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.content.ContentSeeder
import com.compose.wonderlearn.data.content.ContentSource
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.shared.ContentManifest
import com.compose.wonderlearn.shared.ManifestCategory
import com.compose.wonderlearn.shared.ManifestWord
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentSeederTest {

  private val manifestV2 = ContentManifest(
    version = 2,
    categories = listOf(ManifestCategory("fruits", "Fruits", "🍎", 0)),
    words = listOf(
      ManifestWord("apple", "fruits", "🍎", mapOf("en" to "Apple", "hy" to "Խնձոր", "ru" to "Яблоко")),
    ),
  )

  private fun newSeeder(dispatcher: CoroutineDispatcher): ContentSeeder {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    return ContentSeeder(WonderLearnDatabase(driver), dispatcher)
  }

  /** A source that suspends before returning, so two syncs are in flight at the same time. */
  private fun suspendingSource(manifest: ContentManifest) = object : ContentSource {
    override suspend fun load(): ContentManifest {
      yield()
      return manifest
    }
  }

  @Test
  fun concurrentSyncsApplyTheManifestExactlyOnce() = runTest {
    val seeder = newSeeder(UnconfinedTestDispatcher(testScheduler))
    val source = suspendingSource(manifestV2)

    val a = async { seeder.sync(source) }
    val b = async { seeder.sync(source) }
    val applied = listOf(a.await(), b.await()).count { it }

    assertEquals(1, applied, "one sync applies version 2; the other sees it already current")
    assertEquals(2L, seeder.storedVersion(), "content version settled at the manifest version")
  }

  @Test
  fun aSecondSyncOfTheSameVersionDoesNothing() = runTest {
    val seeder = newSeeder(UnconfinedTestDispatcher(testScheduler))
    val source = suspendingSource(manifestV2)

    assertEquals(true, seeder.sync(source), "first sync applies")
    assertEquals(false, seeder.sync(source), "second sync of the same version is a no-op")
  }
}
