package com.pbl9.pomodoro.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

fun timerFlow(countSeconds: Long,
              initialOffsetSeconds: Long = 0L) = flow {
    val startTimestamp: Long = System.currentTimeMillis()
    val endTimestamp = startTimestamp + countSeconds * 1000 - initialOffsetSeconds * 1000L
    var secondsRemained = Math.round((endTimestamp - startTimestamp) / 1000.0)
    while (currentCoroutineContext().isActive && secondsRemained >= 0) {
        emit(secondsRemained)
        delay(1000L)
        secondsRemained = Math.round((endTimestamp - System.currentTimeMillis()) / 1000.0)
    }
}.flowOn(Dispatchers.Default)