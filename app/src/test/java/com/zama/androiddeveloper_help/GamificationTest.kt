package com.zama.androiddeveloper_help

import com.zama.androiddeveloper_help.data.repetition.SpacedRepetition
import com.zama.androiddeveloper_help.gamification.Achievements
import com.zama.androiddeveloper_help.gamification.Gamification
import com.zama.androiddeveloper_help.data.domain.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationTest {

    @Test
    fun `level grows with xp`() {
        assertEquals(1, Gamification.level(0))
        assertEquals(2, Gamification.level(50))
        assertEquals(3, Gamification.level(200))
        assertTrue(Gamification.level(10_000) > 10)
    }

    @Test
    fun `level progress stays in bounds`() {
        for (xp in listOf(0L, 30L, 50L, 120L, 300L, 4_000L)) {
            val progress = Gamification.levelProgress(xp)
            assertTrue("xp=$xp", progress >= 0f && progress <= 1f)
        }
    }

    @Test
    fun `xp to next level non negative`() {
        assertEquals(Gamification.xpForLevel(Gamification.level(50) + 1) - 50, Gamification.xpToNextLevel(50))
        for (xp in listOf(0L, 30L, 50L, 120L, 300L, 4_000L)) {
            assertTrue("xp=$xp", Gamification.xpToNextLevel(xp) >= 0)
        }
    }

    @Test
    fun `session xp correct answers plus bonuses`() {
        assertEquals(115L, Gamification.sessionXp(10, 10, 0)) // 10*10 + perfect bonus 15
        assertEquals(60L, Gamification.sessionXp(5, 10, 0)) // 5*10 + 5*2
        assertEquals(64L, Gamification.sessionXp(5, 10, 2)) // + streak bonus 2*2
    }

    @Test
    fun `session xp zero for empty session`() {
        assertEquals(0L, Gamification.sessionXp(0, 0, 3))
    }

    @Test
    fun `achievements respect counters`() {
        val empty = UserProfile()
        assertTrue(Achievements.newlyReached(empty, emptySet()).isEmpty())
        val profile = UserProfile(totalXp = 600, totalCorrect = 120, currentStreak = 5, longestStreak = 5, totalSessions = 2, cardsCreated = 6)
        val reached = Achievements.newlyReached(profile, emptySet()).map { it.id }.toSet()
        assertTrue("xp_500" in reached)
        assertTrue("correct_100" in reached)
        assertTrue("streak_3" in reached)
        assertTrue("cards_5" in reached)
        assertTrue("xp_2000" !in reached)
    }
}

class SpacedRepetitionTest {

    private val now = 1_000_000L
    private val day = 24 * 60 * 60_000L

    @Test
    fun `first correct answer schedules tomorrow`() {
        val item = SpacedRepetition.review(SpacedRepetitionItem(), correct = true, nowMillis = now)
        assertEquals(1, item.streak)
        assertEquals(1, item.intervalDays)
        assertEquals(now + day, item.dueAtMillis)
    }

    @Test
    fun `streak grows interval ladder`() {
        var item = SpacedRepetitionItem()
        item = SpacedRepetition.review(item, true, now)
        item = SpacedRepetition.review(item, true, now)
        assertEquals(2, item.streak)
        assertEquals(2, item.intervalDays)
        item = SpacedRepetition.review(item, true, now)
        assertEquals(3, item.streak)
        assertEquals(6, item.intervalDays)
    }

    @Test
    fun `incorrect answer resets and schedules soon`() {
        val item = SpacedRepetition.review(
            SpacedRepetitionItem(streak = 5, intervalDays = 20, easeFactor = 2.5),
            correct = false,
            nowMillis = now
        )
        assertEquals(0, item.streak)
        assertEquals(0, item.intervalDays)
        assertTrue(item.dueAtMillis in (now + 1_000L)..(now + day)) // через 10 минут
        assertEquals(2.3, item.easeFactor, 0.001) // 2.5 - 0.2
    }

    @Test
    fun `ease factor clamped`() {
        var item = SpacedRepetitionItem(easeFactor = 1.35)
        item = SpacedRepetition.review(item, false, now)
        assertEquals(1.3, item.easeFactor, 0.001)
    }
}

private fun SpacedRepetitionItem(
    streak: Int = 0,
    intervalDays: Int = 0,
    easeFactor: Double = 2.5,
    dueAtMillis: Long = 0
): com.zama.androiddeveloper_help.data.repetition.SrsItem =
    com.zama.androiddeveloper_help.data.repetition.SrsItem(
        streak = streak, intervalDays = intervalDays, easeFactor = easeFactor, dueAtMillis = dueAtMillis
    )
