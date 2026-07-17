package com.compose.wonderlearn.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Dispatchers.IO is internal on Kotlin/Native; Default is the recommended
// background dispatcher there, and the native SQLite driver manages its own
// connection threading.
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
