package com.zama.androiddeveloper_help.gamification

/**
 * Ядро геймификации: XP за действия, уровни и пороги.
 */
object Gamification {

    const val XP_PER_CORRECT = 10L
    const val XP_PER_INCORRECT = 2L
    const val XP_PER_CARD_CREATE = 5L
    const val XP_SESSION_BONUS = 15L
    const val XP_STREAK_BONUS_PER_DAY = 2L // бонус за стрик: streak * 2 XP

    /** XP за верные ответы сессии + бонус за завершение и стрик. */
    fun sessionXp(correctCount: Int, totalCount: Int, currentStreak: Int): Long {
        if (totalCount == 0) return 0
        val answerXp = correctCount * XP_PER_CORRECT + (totalCount - correctCount) * XP_PER_INCORRECT
        val percentBonus = if (totalCount > 0 && correctCount == totalCount) XP_SESSION_BONUS else 0L
        return answerXp + percentBonus + currentStreak * XP_STREAK_BONUS_PER_DAY
    }

    /** Уровень растёт по корню: уровень 1 с 0 XP, каждый следующий требует всё больше. */
    fun level(totalXp: Long): Int = (kotlin.math.sqrt(totalXp.toDouble() / 50.0)).toInt() + 1

    /** Сколько XP нужно набрать с текущего уровня до следующего. */
    fun xpForLevel(level: Int): Long = (level - 1).toLong() * (level - 1) * 50

    /** Прогресс (0..1) до следующего уровня. */
    fun levelProgress(totalXp: Long): Float {
        val current = level(totalXp)
        val startXp = xpForLevel(current)
        val nextXp = xpForLevel(current + 1)
        val span = nextXp - startXp
        if (span <= 0) return 0f
        return ((totalXp - startXp).toFloat() / span).coerceIn(0f, 1f)
    }

    /** XP, оставшееся до следующего уровня. */
    fun xpToNextLevel(totalXp: Long): Long {
        val current = level(totalXp)
        return (xpForLevel(current + 1) - totalXp).coerceAtLeast(0)
    }
}
