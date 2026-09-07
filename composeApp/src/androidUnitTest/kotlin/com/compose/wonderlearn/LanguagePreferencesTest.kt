package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.data.SqlDelightLanguagePreferences
import com.compose.wonderlearn.data.SqlDelightProfileRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.Language
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanguagePreferencesTest {

  private class Fixture(
    val languagePrefs: SqlDelightLanguagePreferences,
    val profiles: SqlDelightProfileRepository,
    val db: WonderLearnDatabase,
  )

  /** Builds the repositories over a fresh in-memory DB with no profile rows and no legacy flat
   * keys yet — callers seed whichever of those a given test needs before constructing
   * [SqlDelightLanguagePreferences], since its one-time flat-key migration runs synchronously
   * in its `init` block. */
  private fun newFixture(): Fixture {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val db = WonderLearnDatabase(driver)
    val dispatcher = UnconfinedTestDispatcher()
    val profiles = SqlDelightProfileRepository(db, dispatcher)
    val languagePrefs = SqlDelightLanguagePreferences(db, profiles, dispatcher)
    return Fixture(languagePrefs, profiles, db)
  }

  @Test
  fun twoProfilesHaveIndependentLanguagePairs() = runTest {
    val f = newFixture()
    val kidA = f.profiles.createProfile("Kid A", null)
    val kidB = f.profiles.createProfile("Kid B", null)

    f.profiles.setActiveProfile(kidA.id)
    f.languagePrefs.setNativeLanguage(Language.ARMENIAN)
    f.languagePrefs.setTargetLanguage(Language.ENGLISH)

    f.profiles.setActiveProfile(kidB.id)
    f.languagePrefs.setNativeLanguage(Language.RUSSIAN)
    f.languagePrefs.setTargetLanguage(Language.ARMENIAN)

    f.profiles.setActiveProfile(kidA.id)
    assertEquals(Language.ARMENIAN, f.languagePrefs.nativeLanguage().first(), "Kid A's native language must be unaffected by Kid B's")
    assertEquals(Language.ENGLISH, f.languagePrefs.targetLanguage().first(), "Kid A's target language must be unaffected by Kid B's")

    f.profiles.setActiveProfile(kidB.id)
    assertEquals(Language.RUSSIAN, f.languagePrefs.nativeLanguage().first())
    assertEquals(Language.ARMENIAN, f.languagePrefs.targetLanguage().first())
  }

  @Test
  fun switchingActiveProfileSwitchesWhichPairIsRead() = runTest {
    val f = newFixture()
    val kidA = f.profiles.createProfile("Kid A", null)
    val kidB = f.profiles.createProfile("Kid B", null)

    f.profiles.setActiveProfile(kidA.id)
    f.languagePrefs.setNativeLanguage(Language.ENGLISH)
    assertEquals(Language.ENGLISH, f.languagePrefs.nativeLanguage().first())

    f.profiles.setActiveProfile(kidB.id)
    assertNull(f.languagePrefs.nativeLanguage().first(), "a fresh profile has no language pair of its own yet")

    f.profiles.setActiveProfile(kidA.id)
    assertEquals(Language.ENGLISH, f.languagePrefs.nativeLanguage().first(), "switching back must read Kid A's own value again")
  }

  @Test
  fun newProfileInheritsTheActiveProfilesLanguageAtCreation() = runTest {
    // Mirrors AccountViewModel.addChild: read the outgoing active profile's language *before*
    // switching, create + switch to the new profile, then write the inherited values onto it —
    // so the new profile never sits on a null pair that would wrongly re-trigger onboarding.
    val f = newFixture()
    val parent = f.profiles.createProfile("Parent", null)
    f.profiles.setActiveProfile(parent.id)
    f.languagePrefs.setNativeLanguage(Language.RUSSIAN)
    f.languagePrefs.setTargetLanguage(Language.ARMENIAN)

    val inheritedNative = f.languagePrefs.nativeLanguage().first()
    val inheritedTarget = f.languagePrefs.targetLanguage().first()
    val child = f.profiles.createProfile("New Kid", null)
    f.profiles.setActiveProfile(child.id)
    inheritedNative?.let { f.languagePrefs.setNativeLanguage(it) }
    inheritedTarget?.let { f.languagePrefs.setTargetLanguage(it) }

    assertEquals(Language.RUSSIAN, f.languagePrefs.nativeLanguage().first(), "new profile inherits the previously-active profile's native language")
    assertEquals(Language.ARMENIAN, f.languagePrefs.targetLanguage().first(), "new profile inherits the previously-active profile's target language")

    // And the parent's own pair must be untouched by the child's inherited copy.
    f.profiles.setActiveProfile(parent.id)
    assertEquals(Language.RUSSIAN, f.languagePrefs.nativeLanguage().first())
    assertEquals(Language.ARMENIAN, f.languagePrefs.targetLanguage().first())
  }

  @Test
  fun migrationCopiesTheLegacyFlatValueOntoAProfileWithoutItsOwnKey() = runTest {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val db = WonderLearnDatabase(driver)
    val dispatcher = UnconfinedTestDispatcher()
    val queries = db.wonderLearnQueries
    val profiles = SqlDelightProfileRepository(db, dispatcher)

    // Simulate a pre-upgrade install: a single flat, unscoped language pair and one existing
    // profile row, with no per-profile key of its own yet.
    val existing = profiles.createProfile("Existing Kid", null)
    queries.upsertSetting("nativeLanguage", Language.ARMENIAN.code)
    queries.upsertSetting("language", Language.ENGLISH.code)

    // The migration runs synchronously in the constructor's init block.
    val languagePrefs = SqlDelightLanguagePreferences(db, profiles, dispatcher)

    profiles.setActiveProfile(existing.id)
    assertEquals(Language.ARMENIAN, languagePrefs.nativeLanguage().first(), "the flat native language must be copied onto the existing profile")
    assertEquals(Language.ENGLISH, languagePrefs.targetLanguage().first(), "the flat target language must be copied onto the existing profile")
  }

  @Test
  fun migrationDoesNotOverwriteAProfilesOwnExistingKey() = runTest {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val db = WonderLearnDatabase(driver)
    val dispatcher = UnconfinedTestDispatcher()
    val queries = db.wonderLearnQueries
    val profiles = SqlDelightProfileRepository(db, dispatcher)

    val kid = profiles.createProfile("Kid", null)
    // This profile already made its own per-profile choice before the legacy flat key existed
    // (or before this run of the migration) — the migration must never clobber it.
    queries.upsertSetting("nativeLanguage:${kid.id}", Language.RUSSIAN.code)
    queries.upsertSetting("nativeLanguage", Language.ARMENIAN.code)

    val languagePrefs = SqlDelightLanguagePreferences(db, profiles, dispatcher)

    profiles.setActiveProfile(kid.id)
    assertEquals(Language.RUSSIAN, languagePrefs.nativeLanguage().first(), "migration must not overwrite a profile's own already-set language")
  }
}
