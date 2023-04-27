package com.pbl9.pomodoro.repository

import com.pbl9.pomodoro.db.JournalDao
import com.pbl9.pomodoro.models.db.JournalEntity
import com.pbl9.pomodoro.models.db.JournalEvent

class JournalRepository(private val journalDao: JournalDao) {

    suspend fun saveEvent(event: JournalEvent) {
        val lastEntity = journalDao.getLastEntity()
        val entity = if(lastEntity == null) { //database is empty, start new session
            if(event == JournalEvent.SESSION_START) JournalEntity(event = event, sessionNumber = 1, elapsedTimeMillis = 0)
            else throw IllegalStateException("Illegall state, database is empty so first event must be SESSION_START")
        } else {
            createNewEntityBasedOnPrevious(event, lastEntity)
        }
        journalDao.addEntity(entity)
    }

    suspend fun resume() {
        val lastEntity = journalDao.getLastEntity() ?: return
        lastEntity.previousEvent?.let {
            when (it) {
                JournalEvent.SESSION_RESUME, JournalEvent.SESSION_START -> {
                    JournalEntity(event = JournalEvent.SESSION_RESUME, sessionNumber = lastEntity.sessionNumber, elapsedTimeMillis = lastEntity.elapsedTimeMillis, previousEvent = JournalEvent.PAUSE)
                }
                JournalEvent.BREAK_START, JournalEvent.BREAK_RESUME -> {
                    JournalEntity(event = JournalEvent.BREAK_RESUME, sessionNumber = lastEntity.sessionNumber, elapsedTimeMillis = lastEntity.elapsedTimeMillis, previousEvent = JournalEvent.PAUSE)
                }
                else -> {
                    null
                }
            }
        }?.let {
            journalDao.addEntity(it)
        }
    }

    fun getLastJournalFlow() = journalDao.getLastEntityFlow()

    private fun createNewEntityBasedOnPrevious(
        eventToSave: JournalEvent,
        previous: JournalEntity
    ) =
        when (eventToSave) {
            JournalEvent.SESSION_START -> {
                JournalEntity(
                    event = eventToSave,
                    sessionNumber = previous.sessionNumber + 1,
                    elapsedTimeMillis = 0,
                    previousEvent = previous.event
                )
            }
            JournalEvent.BREAK_START -> {
                JournalEntity(
                    event = eventToSave,
                    sessionNumber = previous.sessionNumber,
                    elapsedTimeMillis = 0,
                    previousEvent = previous.event
                )
            }
            JournalEvent.PAUSE -> {
                val now = System.currentTimeMillis()
                val newElapsedTime = previous.elapsedTimeMillis + now - previous.timestamp
                JournalEntity(
                    event = eventToSave,
                    sessionNumber = previous.sessionNumber,
                    elapsedTimeMillis = newElapsedTime,
                    previousEvent = previous.event
                )
            }
            JournalEvent.SESSION_RESUME -> {
                JournalEntity(
                    event = eventToSave,
                    sessionNumber = previous.sessionNumber,
                    elapsedTimeMillis = previous.elapsedTimeMillis,
                    previousEvent = previous.event
                )
            }
            JournalEvent.BREAK_RESUME -> {
                JournalEntity(
                    event = eventToSave,
                    sessionNumber = previous.sessionNumber,
                    elapsedTimeMillis = previous.elapsedTimeMillis,
                    previousEvent = previous.event
                )
            }
            JournalEvent.WORK_END -> {
                JournalEntity(
                    event = eventToSave,
                    sessionNumber = 0,
                    previousEvent = previous.event,
                    elapsedTimeMillis = 0,
                )
            }
        }


}
/*
* 1. Start -> timestamp = 0, elapsedTime = 0
* 2. Pause -> timestamp = x, elapsedTime = x
* 3. Start -> timestamp = y, elapsedTime = x
* 4. Pause -> timestamp = y + z, elapsedTime = x + z - y
* */