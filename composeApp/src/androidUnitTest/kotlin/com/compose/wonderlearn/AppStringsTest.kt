package com.compose.wonderlearn

import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.LocalizedString
import kotlin.test.Test
import kotlin.test.assertTrue

class AppStringsTest {

  @Test
  fun everyStringCoversEveryNativeLanguage() {
    val entries = AppStrings.javaClass.declaredMethods
      .filter { it.parameterCount == 0 && it.returnType == LocalizedString::class.java }
    assertTrue(entries.isNotEmpty(), "no AppStrings entries found via reflection")
    entries.forEach { getter ->
      val string = getter.invoke(AppStrings) as LocalizedString
      val missing = Language.natives.toSet() - string.languages
      assertTrue(missing.isEmpty(), "${getter.name} is missing translations for $missing")
    }
  }
}
