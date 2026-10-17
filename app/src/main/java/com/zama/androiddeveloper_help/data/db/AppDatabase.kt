package com.zama.androiddeveloper_help.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        QuestionProgressEntity::class,
        UserDeckEntity::class,
        UserCardEntity::class,
        UserProfileEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun questionProgressDao(): QuestionProgressDao
    abstract fun userDeckDao(): UserDeckDao
    abstract fun userCardDao(): UserCardDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "android_dev_help.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
