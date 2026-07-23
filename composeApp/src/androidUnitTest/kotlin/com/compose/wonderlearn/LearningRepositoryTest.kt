package com.compose.wonderlearn

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.compose.wonderlearn.shared.ContentManifest
import com.compose.wonderlearn.data.content.ContentSeeder
import com.compose.wonderlearn.data.SqlDelightLearningRepository
import com.compose.wonderlearn.data.SqlDelightProfileRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.DEFAULT_PROFILE_ID
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.QuizMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LearningRepositoryTest {

  private val manifest: ContentManifest by lazy {
    val file = listOf(
      File("src/commonMain/composeResources/files/content/vocabulary.json"),
      File("composeApp/src/commonMain/composeResources/files/content/vocabulary.json"),
    ).firstOrNull { it.exists() } ?: error("vocabulary manifest not found from ${File(".").absolutePath}")
    Json { ignoreUnknownKeys = true }.decodeFromString(file.readText())
  }

  private val hy = Language.ARMENIAN
  private val en = Language.ENGLISH

  private class Fixture(
    val repo: SqlDelightLearningRepository,
    val profiles: SqlDelightProfileRepository,
  )

  private fun newFixture(dispatcher: CoroutineDispatcher): Fixture {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    WonderLearnDatabase.Schema.create(driver)
    val database = WonderLearnDatabase(driver)
    ContentSeeder(database, dispatcher).apply(manifest)
    val profiles = SqlDelightProfileRepository(database, dispatcher)
    return Fixture(SqlDelightLearningRepository(database, profiles, dispatcher), profiles)
  }

  private fun newRepository(dispatcher: CoroutineDispatcher): SqlDelightLearningRepository =
    newFixture(dispatcher).repo

  @Test
  fun bundledManifestIsInternallyConsistent() {
    assertTrue(manifest.categories.isNotEmpty(), "manifest has categories")
    assertTrue(manifest.words.size >= 50, "manifest has the full vocabulary, got ${manifest.words.size}")

    val categoryIds = manifest.categories.map { it.id }.toSet()
    val orphans = manifest.words.filter { it.categoryId !in categoryIds }
    assertTrue(orphans.isEmpty(), "words point at missing categories: ${orphans.map { it.id }}")

    val codes = Language.entries.map { it.code }.toSet()
    val untranslated = manifest.words.filter { !it.translations.keys.containsAll(codes) }
    assertTrue(untranslated.isEmpty(), "words missing a translation: ${untranslated.map { it.id }}")

    val duplicates = manifest.words.groupBy { it.id }.filterValues { it.size > 1 }.keys
    assertTrue(duplicates.isEmpty(), "duplicate word ids: $duplicates")
  }

  @Test
  fun wordBecomesLearnedAfterThreeCorrectInARow() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    assertFalse(repo.recordCorrect("apple", hy), "streak 1 is not learned")
    assertFalse(repo.recordCorrect("apple", hy), "streak 2 is not learned")
    assertTrue(repo.recordCorrect("apple", hy), "streak 3 becomes learned")
    assertTrue(repo.learnedItems(hy).first().any { it.id == "apple" })
  }

  @Test
  fun wrongAnswerResetsTheStreak() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repo.recordCorrect("apple", hy)
    repo.recordCorrect("apple", hy)
    repo.recordWrong("apple", hy, QuizMode.LEARN)
    assertFalse(repo.recordCorrect("apple", hy), "after reset, one correct is streak 1")
    assertTrue(repo.learnedItems(hy).first().none { it.id == "apple" })
  }

  @Test
  fun quizNeverTargetsALearnedWord() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }
    repeat(60) {
      val round = repo.nextRound(hy, QuizMode.LEARN)
      assertNotNull(round)
      assertTrue(round.target.id != "apple", "learned word must not be a quiz target")
    }
  }

  @Test
  fun nextRoundIsNullWhenEverythingIsLearned() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    manifest.words.forEach { word -> repeat(3) { repo.recordCorrect(word.id, hy) } }
    assertNull(repo.nextRound(hy, QuizMode.LEARN), "no rounds left once all words are learned")
  }

  @Test
  fun reviseQuizzesOnlyLearnedWords() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }
    repeat(3) { repo.recordCorrect("dog", hy) }

    repeat(40) {
      val round = repo.nextRound(hy, QuizMode.REVISE)
      assertNotNull(round)
      assertTrue(
        round.target.id == "apple" || round.target.id == "dog",
        "revise must only target learned words, got ${'$'}{round.target.id}",
      )
    }
  }

  @Test
  fun reviseIsEmptyUntilSomethingIsLearned() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    assertNull(repo.nextRound(hy, QuizMode.REVISE), "nothing learned yet, nothing to revise")
  }

  @Test
  fun aWrongReviseAnswerCostsOneStepNotTheWholeStreak() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }
    assertTrue(repo.learnedItems(hy).first().any { it.id == "apple" })

    repo.recordWrong("apple", hy, QuizMode.REVISE)
    assertTrue(
      repo.learnedItems(hy).first().none { it.id == "apple" },
      "3 -> 2 drops it out of learned",
    )

    assertTrue(
      repo.recordCorrect("apple", hy),
      "one correct answer takes 2 -> 3 and re-masters it",
    )
  }

  @Test
  fun aWrongLearnAnswerStillClearsTheWholeStreak() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }

    repo.recordWrong("apple", hy, QuizMode.LEARN)
    assertFalse(repo.recordCorrect("apple", hy), "streak was cleared, one correct is only 1")
    assertFalse(repo.recordCorrect("apple", hy), "still only 2")
    assertTrue(repo.recordCorrect("apple", hy), "3 again")
  }

  @Test
  fun progressInOneLanguageDoesNotLeakIntoAnother() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }

    assertTrue(repo.learnedItems(hy).first().any { it.id == "apple" }, "learned in Armenian")
    assertTrue(repo.learnedItems(en).first().none { it.id == "apple" }, "not learned in English")
  }

  @Test
  fun theSameWordIsStillQuizzedInALanguageWhereItIsNotLearned() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { repo.recordCorrect("apple", hy) }

    var targetedInEnglish = false
    repeat(200) {
      val round = repo.nextRound(en, QuizMode.LEARN)
      assertNotNull(round)
      if (round.target.id == "apple") targetedInEnglish = true
    }
    assertTrue(targetedInEnglish, "a word learned in Armenian must still come up in English")
  }

  @Test
  fun progressInOneProfileDoesNotLeakIntoAnother() = runTest {
    val fixture = newFixture(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { fixture.repo.recordCorrect("apple", hy) }
    assertTrue(fixture.repo.learnedItems(hy).first().any { it.id == "apple" })

    val sibling = fixture.profiles.createProfile("Sibling")
    fixture.profiles.setActiveProfile(sibling.id)

    assertTrue(
      fixture.repo.learnedItems(hy).first().none { it.id == "apple" },
      "a second child starts from nothing",
    )
    assertFalse(
      fixture.repo.recordCorrect("apple", hy),
      "the sibling's first correct answer is streak 1, not 4",
    )
  }

  @Test
  fun switchingBackRestoresTheOriginalProfileProgress() = runTest {
    val fixture = newFixture(UnconfinedTestDispatcher(testScheduler))
    repeat(3) { fixture.repo.recordCorrect("apple", hy) }

    val sibling = fixture.profiles.createProfile("Sibling")
    fixture.profiles.setActiveProfile(sibling.id)
    repeat(3) { fixture.repo.recordCorrect("dog", hy) }
    fixture.profiles.setActiveProfile(DEFAULT_PROFILE_ID)

    val learned = fixture.repo.learnedItems(hy).first().map { it.id }
    assertTrue("apple" in learned, "the first profile keeps what it learned")
    assertTrue("dog" !in learned, "and does not inherit the sibling's progress")
  }

  @Test
  fun deletingAProfileRemovesItsProgressAndDoesNotOrphanRows() = runTest {
    val fixture = newFixture(UnconfinedTestDispatcher(testScheduler))
    val keep = fixture.profiles.createProfile("Keeper")
    val child = fixture.profiles.createProfile("Aram")
    fixture.profiles.setActiveProfile(child.id)
    repeat(3) { fixture.repo.recordCorrect("apple", hy) }
    assertTrue(fixture.repo.learnedItems(hy).first().any { it.id == "apple" })

    assertTrue(fixture.profiles.deleteProfile(child.id), "a non-last profile is deletable")

    // active fell back to the remaining profile, which never learned apple
    assertTrue(fixture.profiles.currentProfileId() == keep.id)
    assertTrue(fixture.repo.learnedItems(hy).first().none { it.id == "apple" })
    // and the deleted child's progress rows are gone, not orphaned
    assertFalse(
      fixture.repo.recordCorrect("apple", hy),
      "the surviving profile starts apple from streak 1, proving no stale rows remained",
    )
  }

  @Test
  fun theLastProfileCannotBeDeleted() = runTest {
    val fixture = newFixture(UnconfinedTestDispatcher(testScheduler))
    val only = fixture.profiles.createProfile("Only")
    assertFalse(
      fixture.profiles.deleteProfile(only.id),
      "refusing to delete the only profile leaves the app with an active profile",
    )
    assertTrue(fixture.profiles.profiles().first().any { it.id == only.id })
  }

  @Test
  fun deletingTheActiveProfileReassignsActive() = runTest {
    val fixture = newFixture(UnconfinedTestDispatcher(testScheduler))
    fixture.profiles.createProfile("Keeper")
    val child = fixture.profiles.createProfile("Nare")
    fixture.profiles.setActiveProfile(child.id)

    fixture.profiles.deleteProfile(child.id)

    assertTrue(
      fixture.profiles.currentProfileId() != child.id,
      "active profile no longer points at the deleted one",
    )
    assertTrue(fixture.profiles.profiles().first().none { it.id == child.id })
  }

  @Test
  fun renamingAProfileChangesItsDisplayName() = runTest {
    val fixture = newFixture(UnconfinedTestDispatcher(testScheduler))
    val child = fixture.profiles.createProfile("Typo")
    fixture.profiles.renameProfile(child.id, "Aram")
    val renamed = fixture.profiles.profiles().first().first { it.id == child.id }
    assertTrue(renamed.displayName == "Aram")
  }

  @Test
  fun everythingLearnedInOneLanguageLeavesOtherLanguagesUntouched() = runTest {
    val repo = newRepository(UnconfinedTestDispatcher(testScheduler))
    manifest.words.forEach { word -> repeat(3) { repo.recordCorrect(word.id, hy) } }

    assertNull(repo.nextRound(hy, QuizMode.LEARN), "Armenian is complete")
    assertNotNull(repo.nextRound(en, QuizMode.LEARN), "English still has everything to learn")
  }
}
