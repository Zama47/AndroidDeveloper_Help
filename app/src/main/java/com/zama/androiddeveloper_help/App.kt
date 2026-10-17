package com.zama.androiddeveloper_help

import android.app.Application
import com.zama.androiddeveloper_help.data.db.AppDatabase
import com.zama.androiddeveloper_help.data.repository.DeckRepository
import com.zama.androiddeveloper_help.data.repository.ProfileRepository
import com.zama.androiddeveloper_help.data.repository.ProgressRepository

/** Простой DI-контейнер без внешних библиотек. */
class App : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var progressRepository: ProgressRepository
        private set
    lateinit var deckRepository: DeckRepository
        private set
    lateinit var profileRepository: ProfileRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.create(this)
        progressRepository = ProgressRepository(database.questionProgressDao())
        deckRepository = DeckRepository(database.userDeckDao(), database.userCardDao())
        profileRepository = ProfileRepository(database.userProfileDao(), database.achievementDao())
    }
}
