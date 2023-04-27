package com.pbl9.pomodoro.models.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "session_number") val sessionNumber: Int,
    @ColumnInfo(name = "event") val event: JournalEvent,
    @ColumnInfo(name = "elapsed_time") val elapsedTimeMillis: Long,
    @ColumnInfo(name = "prevoius_event") val previousEvent: JournalEvent? = null
)