package com.compose.wonderlearn.data

import kotlinx.coroutines.CoroutineDispatcher

/** Dispatcher for blocking database I/O (Dispatchers.IO is JVM/Native-only, not in common). */
expect val ioDispatcher: CoroutineDispatcher
