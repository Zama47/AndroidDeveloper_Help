package com.zama.androiddeveloper_help.gamification

import com.zama.androiddeveloper_help.data.domain.UserProfile

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String
) {
    fun isReached(profile: UserProfile): Boolean = when (id) {
        "first_quiz" -> profile.totalSessions >= 1
        "quiz_10" -> profile.totalSessions >= 10
        "correct_10" -> profile.totalCorrect >= 10
        "correct_50" -> profile.totalCorrect >= 50
        "correct_100" -> profile.totalCorrect >= 100
        "streak_3" -> profile.longestStreak >= 3
        "streak_7" -> profile.longestStreak >= 7
        "xp_500" -> profile.totalXp >= 500
        "xp_2000" -> profile.totalXp >= 2000
        "cards_5" -> profile.cardsCreated >= 5
        "cards_20" -> profile.cardsCreated >= 20
        else -> false
    }
}

object Achievements {

    val ALL: List<AchievementDef> = listOf(
        AchievementDef("first_quiz", "Первый шаг", "Завершите первую сессию", "🚀"),
        AchievementDef("quiz_10", "Марафонец", "Завершите 10 сессий", "🏃"),
        AchievementDef("correct_10", "Разминка", "10 верных ответов", "✅"),
        AchievementDef("correct_50", "Уверенный старт", "50 верных ответов", "🎯"),
        AchievementDef("correct_100", "Сотка", "100 верных ответов", "💯"),
        AchievementDef("perfect_session", "Без промаха", "Сессия со 100% результатом", "🏆"),
        AchievementDef("streak_3", "Втянулся", "Стрик 3 дня подряд", "🔥"),
        AchievementDef("streak_7", "Недельная привычка", "Стрик 7 дней подряд", "📅"),
        AchievementDef("xp_500", "Пятьсот", "500 XP всего", "⚡"),
        AchievementDef("xp_2000", "Гуру", "2000 XP всего", "🧠"),
        AchievementDef("cards_5", "Автор", "Создайте 5 карточек", "✍️"),
        AchievementDef("cards_20", "Писатель", "Создайте 20 карточек", "📚")
    )

    const val PERFECT_SESSION = "perfect_session"

    /** Найденные, но ещё не разблокированные достижения профиля. */
    fun newlyReached(profile: UserProfile, unlockedIds: Set<String>): List<AchievementDef> =
        ALL.filter { it.id !in unlockedIds && it.isReached(profile) }
}
