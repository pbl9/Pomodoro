package com.pbl9.pomodoro.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pbl9.pomodoro.models.db.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal LIMIT 1")
    suspend fun getLastEntity(): JournalEntity?

    @Query("SELECT * FROM journal LIMIT 1")
    fun getLastEntityFlow(): Flow<JournalEntity>

    @Insert
    suspend fun addEntity(entity: JournalEntity)
}