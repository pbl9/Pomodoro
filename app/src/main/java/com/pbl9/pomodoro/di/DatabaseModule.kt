package com.pbl9.pomodoro.di

import android.content.Context
import androidx.room.Room
import com.pbl9.pomodoro.db.JournalDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideJournalDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(context, JournalDatabase::class.java, JournalDatabase.DATABASE_NAME)
        .build()

}