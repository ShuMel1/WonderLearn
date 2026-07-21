package com.compose.wonderlearn.ui

import androidx.compose.runtime.compositionLocalOf
import com.compose.wonderlearn.domain.Language

/** The language being learned — what words are shown, spoken and scored in. */
val LocalLanguage = compositionLocalOf { Language.ENGLISH }

/** The language the child already speaks, used for translations and prompts. */
val LocalNativeLanguage = compositionLocalOf { Language.ENGLISH }
