package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

const val DEFAULT_DAILY_GOAL = 5
const val XP_PER_CORRECT = 10
const val XP_PER_WORD_LEARNED = 50

/** Today's learning at a glance, for the home screen. */
data class DailyProgress(
  val wordsToday: Int = 0,
  val dailyGoal: Int = DEFAULT_DAILY_GOAL,
  val streakDays: Int = 0,
  val totalXp: Int = 0,
) {
  val goalReached: Boolean get() = wordsToday >= dailyGoal
}

/** The current local day as a count of days since the epoch, so streak maths is plain subtraction. */
interface TimeProvider {
  fun todayEpochDay(): Long
}

class SystemTimeProvider : TimeProvider {
  override fun todayEpochDay(): Long =
    Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochDays().toLong()
}

interface ProgressRepository {
  suspend fun recordCorrectAnswer()

  suspend fun recordWordLearned()

  fun dailyProgress(): Flow<DailyProgress>
}

/** Consecutive days with activity, counting back from today (or yesterday if today isn't done yet). */
fun computeStreak(today: Long, activeDaysDescending: List<Long>): Int {
  val days = activeDaysDescending.toHashSet()
  var cursor = when {
    today in days -> today
    (today - 1) in days -> today - 1
    else -> return 0
  }
  var streak = 0
  while (cursor in days) {
    streak++
    cursor--
  }
  return streak
}
