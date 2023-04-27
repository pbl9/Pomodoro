package com.pbl9.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbl9.pomodoro.models.db.JournalEntity
import com.pbl9.pomodoro.models.db.JournalEvent
import com.pbl9.pomodoro.models.states.AppState
import com.pbl9.pomodoro.repository.JournalRepository
import com.pbl9.pomodoro.utils.TimeFormatter
import com.pbl9.pomodoro.utils.timerFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val journalRepository: JournalRepository) :
    ViewModel() {

    private val journal =
        journalRepository.getLastJournalFlow().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _effectFlow = MutableSharedFlow<Effect>()
    val effectFlow: SharedFlow<Effect> get() = _effectFlow

    val appState = journal.map {
        when (it?.event) {
            JournalEvent.SESSION_START -> {
                AppState.Session(it.sessionNumber)
            }
            JournalEvent.BREAK_START -> {
                AppState.Break(it.sessionNumber)
            }
            JournalEvent.SESSION_RESUME -> {
                AppState.Session(it.sessionNumber)
            }
            JournalEvent.BREAK_RESUME -> {
                AppState.Break(it.sessionNumber)
            }
            JournalEvent.PAUSE -> {
                AppState.Pause
            }
            JournalEvent.WORK_END -> {
                AppState.Idle
            }
            else -> {
                AppState.Idle
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        AppState.Idle
    )


    init {
        viewModelScope.launch {
            journal.collectLatest {
                when (it?.event) {
                    JournalEvent.SESSION_START -> {
                        startCounting(countSeconds = SESSION_DURATION_IN_SECONDS)
                    }
                    JournalEvent.BREAK_START -> {
                        startCounting(countSeconds = BREAK_DURATION_IN_SECONDS)
                    }
                    JournalEvent.SESSION_RESUME -> {
                        startCounting(
                            countSeconds = SESSION_DURATION_IN_SECONDS,
                            initialOffset = it.elapsedTimeMillis
                        )
                    }
                    JournalEvent.BREAK_RESUME -> {
                        startCounting(
                            countSeconds = BREAK_DURATION_IN_SECONDS,
                            initialOffset = it.elapsedTimeMillis
                        )
                    }
                    JournalEvent.PAUSE -> {
                        stopCounting()
                        remainedTime.value = TimeFormatter.formatToMinutesAndSeconds(getRemainedSecondsAfterResume(it))
                    }
                    JournalEvent.WORK_END -> {
                        remainedTime.value = "00:00"
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getRemainedSecondsAfterResume(entity: JournalEntity): Long {
        val elapsedTimeSeconds = entity.elapsedTimeMillis / 1000L
        return when(entity.previousEvent) {
            JournalEvent.SESSION_START, JournalEvent.SESSION_RESUME -> SESSION_DURATION_IN_SECONDS - elapsedTimeSeconds
            JournalEvent.BREAK_START, JournalEvent.BREAK_RESUME -> BREAK_DURATION_IN_SECONDS - elapsedTimeSeconds
            else -> 0L
        }
    }

    val remainedTime = MutableStateFlow(
        TimeFormatter.formatToMinutesAndSeconds(
            SESSION_DURATION_IN_SECONDS
        )
    )

    private var tickerJob: Job? = null

    private fun stopCounting() {
        tickerJob?.cancel()
    }

    private fun startCounting(
        countSeconds: Long,
        initialOffset: Long = 0L
    ) {
        tickerJob = viewModelScope.launch {
            timerFlow(countSeconds, initialOffset / 1000).collect {
                remainedTime.value = TimeFormatter.formatToMinutesAndSeconds(it)
            }
            if(isActive) doWhenCountingStops()
        }
    }

    private fun doWhenCountingStops() {
        viewModelScope.launch {
            if(appState.value is AppState.Session) {
                val eventToSave = if(appState.value.sessionNumber < 4) JournalEvent.BREAK_START else JournalEvent.WORK_END
                val effect = if(eventToSave == JournalEvent.BREAK_START) Effect.SESSION_BREAK_END else Effect.WORK_END
                _effectFlow.emit(effect)
                journalRepository.saveEvent(eventToSave)
            } else if(appState.value is AppState.Break) {
                _effectFlow.emit(Effect.SESSION_BREAK_END)
                journalRepository.saveEvent(JournalEvent.SESSION_START)
            }
        }
    }

    fun doAction() {
        viewModelScope.launch {
            when (appState.value) {
                is AppState.Idle -> {
                    journalRepository.saveEvent(JournalEvent.SESSION_START)
                }
                is AppState.Session -> {
                    journalRepository.saveEvent(JournalEvent.PAUSE)
                }
                is AppState.Break -> {
                    journalRepository.saveEvent(JournalEvent.PAUSE)
                }
                is AppState.Pause -> {
                    journalRepository.resume()
                }
            }
        }
    }

    companion object {
        const val SESSION_DURATION_IN_SECONDS = 10L
        const val BREAK_DURATION_IN_SECONDS = 5L//5 * 60L
    }
}