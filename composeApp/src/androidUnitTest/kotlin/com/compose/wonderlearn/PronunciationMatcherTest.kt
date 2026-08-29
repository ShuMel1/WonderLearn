package com.compose.wonderlearn

import com.compose.wonderlearn.speech.PronunciationMatcher
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PronunciationMatcherTest {

  @Test
  fun exactMatchIgnoringCaseAndPunctuation() {
    assertTrue(PronunciationMatcher.matches("Apple", listOf("apple")))
    assertTrue(PronunciationMatcher.matches("apple", listOf("Apple.")))
  }

  @Test
  fun acceptsTheWordInsideAShortPhrase() {
    assertTrue(PronunciationMatcher.matches("apple", listOf("an apple", "a apple please")))
  }

  @Test
  fun stripsLeadingArticlesAcrossLanguages() {
    assertTrue(PronunciationMatcher.matches("chien", listOf("le chien")))
    assertTrue(PronunciationMatcher.matches("hund", listOf("der hund")))
    assertTrue(PronunciationMatcher.matches("gato", listOf("el gato")))
  }

  @Test
  fun ignoresAccents() {
    assertTrue(PronunciationMatcher.matches("café", listOf("cafe")))
    assertTrue(PronunciationMatcher.matches("nino", listOf("niño")))
  }

  @Test
  fun toleratesSmallRecognitionSlips() {
    assertTrue(PronunciationMatcher.matches("elephant", listOf("elefant")))
    assertTrue(PronunciationMatcher.matches("banana", listOf("bananna")))
  }

  @Test
  fun rejectsDifferentWords() {
    assertFalse(PronunciationMatcher.matches("apple", listOf("orange")))
    assertFalse(PronunciationMatcher.matches("dog", listOf("cat")))
    assertFalse(PronunciationMatcher.matches("apple", emptyList()))
    assertFalse(PronunciationMatcher.matches("apple", listOf("")))
  }

  @Test
  fun shortWordsRequireAnExactMatch() {
    assertFalse(PronunciationMatcher.matches("cat", listOf("cap")))
    assertTrue(PronunciationMatcher.matches("cat", listOf("cat")))
  }
}
