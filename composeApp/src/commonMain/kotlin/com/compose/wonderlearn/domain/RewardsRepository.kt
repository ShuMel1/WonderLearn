package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

/** A welcome bounty so a new child can unlock an avatar or two right away. */
const val STARTING_GOLD = 20

/** Gold earned once per finished activity (a completed Quiz set, Memory board, Bubble Pop
 * five-streak) — a first-pass number, easy to retune once real usage is observed. */
const val GOLD_PER_ACTIVITY = 2

/** Number of slots in the check-in reward ladder before it wraps back to the start. */
const val CHECKIN_LADDER_SIZE = 7

/** The deliberately-bigger payout for reaching the last slot of the ladder — a full week in a
 * row — rather than just continuing the day+1 pattern (which would be a modest 8). */
const val CHECKIN_JACKPOT_GOLD = 15

/**
 * Which slot (1-[CHECKIN_LADDER_SIZE]) of the check-in reward ladder [day] falls on: one past
 * whatever slot yesterday was on, if yesterday was claimed; slot 1 otherwise — so a missed day
 * resets the ladder, and a full week completed wraps back to slot 1 rather than climbing forever.
 * [claimedDaysBefore] only needs to contain days strictly before [day].
 */
fun checkInLadderPosition(day: Long, claimedDaysBefore: Set<Long>): Int {
  var consecutive = 0
  var d = day - 1
  while (consecutive < CHECKIN_LADDER_SIZE && d in claimedDaysBefore) {
    consecutive++
    d--
  }
  return (consecutive % CHECKIN_LADDER_SIZE) + 1
}

/**
 * Gold for reaching ladder slot [position] (1-[CHECKIN_LADDER_SIZE]): starts at 2 and climbs by 1
 * each consecutive day, with the last slot a bigger jackpot instead of just continuing the
 * pattern — the size jump is the motivator for coming back every day of the week.
 */
fun checkInRewardForPosition(position: Int): Int =
  if (position >= CHECKIN_LADDER_SIZE) CHECKIN_JACKPOT_GOLD else position + 1

/** Gems earned once per day for clearing the full Daily Adventure path. */
const val GEMS_PER_DAILY_ADVENTURE = 3

/** How much Gold one Gold→Gems exchange costs, and how many Gems it yields. */
const val GOLD_PER_EXCHANGE = 10
const val GEMS_PER_EXCHANGE = 1

/** An avatar a child can wear. Price 0 avatars are free from the start; the rest unlock with Gems. */
data class AvatarItem(
  val emoji: String,
  val price: Int,
)

val AVATARS: List<AvatarItem> = listOf(
  AvatarItem("🦉", 0),
  AvatarItem("🐱", 0),
  AvatarItem("🐶", 0),
  AvatarItem("🦊", 0),
  AvatarItem("🐰", 0),
  AvatarItem("🐼", 0),
  AvatarItem("🦁", 5),
  AvatarItem("🐸", 5),
  AvatarItem("🐵", 7),
  AvatarItem("🐷", 7),
  AvatarItem("🐨", 8),
  AvatarItem("🐯", 8),
  AvatarItem("🐮", 9),
  AvatarItem("🐝", 10),
  AvatarItem("🐧", 12),
  AvatarItem("🐢", 12),
  AvatarItem("🦋", 13),
  AvatarItem("🦄", 15),
  AvatarItem("🐙", 15),
  AvatarItem("🐬", 18),
  AvatarItem("🐳", 23),
  AvatarItem("🦖", 30),
)

/** The avatars a child can wear without spending anything. */
val FREE_AVATARS: List<String> = AVATARS.filter { it.price == 0 }.map { it.emoji }

interface RewardsRepository {
  /** The active child's current spendable Gold balance (earned minus spent). Earned through
   * everyday play — finishing an activity, opening the daily check-in. */
  fun gold(): Flow<Int>

  /** The active child's current spendable Gem balance (earned minus spent). The rarer, more
   * valuable currency — earned only by clearing a full Daily Adventure day. */
  fun gems(): Flow<Int>

  /** Credits Gold for finishing an activity (a Quiz set, a Memory board, a Bubble Pop streak). */
  suspend fun earnGold(amount: Int)

  /** Credits Gems, e.g. for clearing a full Daily Adventure day. */
  suspend fun earnGems(amount: Int)

  /** Emojis of the paid avatars the active child has unlocked (free avatars are always available). */
  fun unlockedAvatars(): Flow<Set<String>>

  /** Unlocks the avatar if affordable in Gems and not already unlocked. Returns true if unlocked. */
  suspend fun unlockAvatar(emoji: String, priceGems: Int): Boolean

  /** Trades [GOLD_PER_EXCHANGE] Gold for [GEMS_PER_EXCHANGE] Gem. False if Gold is insufficient. */
  suspend fun exchangeGoldForGems(): Boolean

  /** Claims today's check-in bonus (see [checkInLadderPosition] for how much) if it hasn't been
   * claimed yet today. True exactly once per day. */
  suspend fun claimDailyCheckIn(): Boolean

  /** Whether today's check-in bonus has already been claimed. */
  fun checkedInToday(): Flow<Boolean>

  /** Today's slot (1-[CHECKIN_LADDER_SIZE]) in the check-in reward ladder — stable whether today
   * has been claimed yet or not, so the popup reads the same before and after claiming. */
  fun checkInLadderPosition(): Flow<Int>
}
