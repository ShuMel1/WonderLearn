package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class AnswerBus {
  private val _events = MutableSharedFlow<Boolean>(extraBufferCapacity = 16)
  val events: SharedFlow<Boolean> = _events

  private val _finished = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
  val finished: SharedFlow<Unit> = _finished

  fun report(correct: Boolean) {
    _events.tryEmit(correct)
  }

  fun reportFinished() {
    _finished.tryEmit(Unit)
  }
}
