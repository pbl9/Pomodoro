package com.pbl9.pomodoro.db

import androidx.room.Dao
import androidx.room.Insert
import com.pbl9.pomodoro.models.db.JournalEntity

@Dao
interface JournalDao {

    @Insert
    fun addEntity(entity: JournalEntity)
}