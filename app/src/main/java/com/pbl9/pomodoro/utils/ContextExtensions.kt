package com.pbl9.pomodoro.utils

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

fun Context.playNotificationSound() {
    val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val r = RingtoneManager.getRingtone(this, notification)
    r.play()
}