package com.zama.androiddeveloper_help.data.repository

import com.zama.androiddeveloper_help.data.db.AchievementDao
import com.zama.androiddeveloper_help.data.db.AchievementEntity
import com.zama.androiddeveloper_help.data.db.UserProfileDao
import com.zama.androiddeveloper_help.data.domain.UserProfile
import com.zama.androiddeveloper_help.data.domain.toProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ProfileRepository(
    private val profileDao: UserProfileDao,
    private val achievementDao: AchievementDao
) {

    val profile: Flow<UserProfile> =
        profileDao.observeProfile().map { it?.toProfile() ?: UserProfile() }

    val achievements: Flow<List<AchievementEntity>> = achievementDao.observeAll()

    suspend fun current(): UserProfile = profileDao.get()?.toProfile() ?: UserProfile()

    suspend fun save(profile: UserProfile) = profileDao.upsert(profile.toEntity())

    suspend fun addXp(amount: Long) {
        val profile = current()
        save(profile.copy(totalXp = profile.totalXp + amount))
    }

    /** Обновляет стрик по локальному дню: +1 если активность вчера, заново если вчера не было. */
    suspend fun touchStreak(today: LocalDate = LocalDate.now()) {
        val profile = current()
        val todayEpochDay = today.toEpochDay()
        val newStreak = when (profile.lastActiveEpochDay) {
            todayEpochDay -> profile.currentStreak
            todayEpochDay - 1 -> profile.currentStreak + 1
            else -> 1
        }
        save(
            profile.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(profile.longestStreak, newStreak),
                lastActiveEpochDay = todayEpochDay
            )
        )
    }

    suspend fun unlockAchievement(id: String, nowMillis: Long = System.currentTimeMillis()) {
        if (achievementDao.get(id) == null) {
            achievementDao.insert(AchievementEntity(id = id, unlockedAt = nowMillis))
        }
    }
}
