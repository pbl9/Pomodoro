package com.pbl9.pomodoro.di

import com.pbl9.pomodoro.db.JournalDatabase
import com.pbl9.pomodoro.repository.JournalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideJournalRepository(journalDatabase: JournalDatabase): JournalRepository {
        return JournalRepository(journalDatabase.journalDao)
    }
}