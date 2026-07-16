package com.compose.wonderlearn.di

import com.compose.wonderlearn.speech.AndroidTextToSpeaker
import com.compose.wonderlearn.speech.TextToSpeaker
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
  single<TextToSpeaker> { AndroidTextToSpeaker(androidContext()) }
}
