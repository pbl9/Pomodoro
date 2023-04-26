package com.pbl9.pomodoro.models.states

import androidx.annotation.StringRes
import com.pbl9.pomodoro.R

sealed class AppState(@StringRes val actionButtonTextRes: Int, @StringRes val stateInfoTextRes: Int, val sessionNumber: Int) {
    object Idle : AppState(actionButtonTextRes = R.string.action_button_start, stateInfoTextRes = R.string.state_info_idle, 0)
    class Session(sessionNumber: Int) : AppState(actionButtonTextRes = R.string.action_button_pause, stateInfoTextRes = R.string.state_info_session, sessionNumber)
    class Break(sessionNumber: Int) : AppState(actionButtonTextRes = R.string.action_button_pause, stateInfoTextRes = R.string.state_info_break, sessionNumber)
    object Pause : AppState(actionButtonTextRes = R.string.action_button_resume, stateInfoTextRes = R.string.state_info_pause, -1)
}