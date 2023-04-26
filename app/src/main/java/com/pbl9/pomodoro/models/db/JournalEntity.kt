package com.pbl9.pomodoro.models.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "session_number") val sessionNumber: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    //event - start of session, end of session, start of break, end of break, pause
) {
}