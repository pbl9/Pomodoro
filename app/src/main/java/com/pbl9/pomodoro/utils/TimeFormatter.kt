package com.pbl9.pomodoro.utils

object TimeFormatter {
    fun formatToMinutesAndSeconds(seconds: Long): String {
        val wholeMinutes = seconds / 60
        val secondsLeft = seconds - wholeMinutes * 60
        return "${wholeMinutes.toString().padStart(2, '0')}:${secondsLeft.toString().padStart(2, '0')}"
    }
}