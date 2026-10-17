package com.zama.androiddeveloper_help.data.repetition

/**
 * Упрощённый SM-2 для интервального повторения:
 * верный ответ растит streak и интервал, неверный сбрасывает прогресс.
 */
data class SrsItem(
    val streak: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val dueAtMillis: Long = 0
)

object SpacedRepetition {

    private const val MINUTE_MS = 60_000L
    private const val DAY_MS = 24 * 60 * 60_000L

    /** Базовая лестница интервалов в днях: 1 → 2 → 6 → рост по ease factor. */
    private fun nextInterval(streak: Int, interval: Int, ease: Double): Int = when (streak) {
        1 -> 1
        2 -> 2
        3 -> 6
        else -> ((interval * ease).toInt()).coerceIn(8, 120)
    }

    fun review(item: SrsItem, correct: Boolean, nowMillis: Long): SrsItem {
        return if (correct) {
            val streak = item.streak + 1
            val ease = (item.easeFactor + 0.05).coerceAtMost(3.0)
            val interval = nextInterval(streak, item.intervalDays, ease)
            item.copy(
                streak = streak,
                intervalDays = interval,
                easeFactor = ease,
                dueAtMillis = nowMillis + interval * DAY_MS
            )
        } else {
            // Неправильный ответ: карточка возвращается в повторение в течение дня.
            item.copy(
                streak = 0,
                intervalDays = 0,
                easeFactor = (item.easeFactor - 0.2).coerceAtLeast(1.3),
                dueAtMillis = nowMillis + 10 * MINUTE_MS
            )
        }
    }
}
