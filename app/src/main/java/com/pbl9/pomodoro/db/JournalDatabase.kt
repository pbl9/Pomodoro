package com.pbl9.pomodoro.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pbl9.pomodoro.models.db.JournalEntity

@Database(entities = [JournalEntity::class], version = 1)
abstract class JournalDatabase: RoomDatabase() {
    abstract val journalDao: JournalDao
    companion object {
        const val DATABASE_NAME = "journal-db"
    }
}