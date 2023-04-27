package com.pbl9.pomodoro.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pbl9.pomodoro.R
import com.pbl9.pomodoro.models.db.JournalEntity
import com.pbl9.pomodoro.models.db.JournalEvent
import com.pbl9.pomodoro.repository.JournalRepository
import com.pbl9.pomodoro.utils.Constants
import com.pbl9.pomodoro.utils.TimeFormatter
import com.pbl9.pomodoro.utils.playNotificationSound
import com.pbl9.pomodoro.utils.timerFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class JournalService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setOngoing(true)

    private var notificationManager: NotificationManager? = null

    @Inject
    lateinit var journalRepository: JournalRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        observeLastJournal()

        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun observeLastJournal() {
        serviceScope.launch {
            journalRepository.getLastJournalFlow().collectLatest {
                when (it.event) {
                    JournalEvent.SESSION_START -> {
                        startCounting(
                            countSeconds = Constants.SESSION_DURATION_IN_SECONDS,
                            initialOffsetMillis = System.currentTimeMillis() - it.timestamp,
                            lasEntity = it
                        )
                    }
                    JournalEvent.BREAK_START -> {
                        startCounting(
                            countSeconds = Constants.BREAK_DURATION_IN_SECONDS,
                            initialOffsetMillis = System.currentTimeMillis() - it.timestamp,
                            lasEntity = it
                        )
                    }
                    JournalEvent.SESSION_RESUME -> {
                        startCounting(
                            countSeconds = Constants.SESSION_DURATION_IN_SECONDS,
                            initialOffsetMillis = it.elapsedTimeMillis,
                            it
                        )
                    }
                    JournalEvent.BREAK_RESUME -> {
                        startCounting(
                            countSeconds = Constants.BREAK_DURATION_IN_SECONDS,
                            initialOffsetMillis = it.elapsedTimeMillis,
                            lasEntity = it
                        )
                    }
                    JournalEvent.PAUSE -> {
                        notification.setContentTitle(getString(R.string.service_pause_title))
                            .setContentText(getString(R.string.service_pause_text))
                        notificationManager?.notify(NOTIFICATION_ID, notification.build())
                    }
                    JournalEvent.WORK_END -> {
                        playNotificationSound()
                        stop()
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_NAME
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            notificationManager?.createNotificationChannel(mChannel)
        }

    }

    private fun startCounting(
        countSeconds: Long,
        initialOffsetMillis: Long = 0L,
        lasEntity: JournalEntity
    ) {
        serviceScope.launch {
            timerFlow(countSeconds, initialOffsetMillis / 1000).collect {
                val notificationContentText = "${getString(R.string.service_session_number_format, lasEntity.sessionNumber)} ${TimeFormatter.formatToMinutesAndSeconds(it)}"
                notification.setContentText(notificationContentText)
                    .setContentTitle(mapJournalEventToNotificationTitle(lasEntity.event)?.let(::getString) ?: "")
                notificationManager?.notify(NOTIFICATION_ID, notification.build())
            }
            if (isActive) doWhenCountingStops(lasEntity)
        }
    }

    private fun doWhenCountingStops(lasEntity: JournalEntity) {
        serviceScope.launch {
            if (lasEntity.event == JournalEvent.SESSION_RESUME || lasEntity.event == JournalEvent.SESSION_START) {
                val eventToSave =
                    if (lasEntity.sessionNumber < 4) JournalEvent.BREAK_START else JournalEvent.WORK_END
                journalRepository.saveEvent(eventToSave)
            } else if (lasEntity.event == JournalEvent.BREAK_START || lasEntity.event == JournalEvent.BREAK_RESUME) {
                journalRepository.saveEvent(JournalEvent.SESSION_START)
            }
        }
    }


    private fun stop() {
        stopForeground(true)
        stopSelf()
    }


    override fun onDestroy() {
        super.onDestroy()
        notificationManager = null
        serviceScope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 78
        const val NOTIFICATION_CHANNEL_ID = "journal"
        const val NOTIFICATION_CHANNEL_NAME = "journal_notification_channel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        fun mapJournalEventToNotificationTitle(event: JournalEvent) = when (event) {
            JournalEvent.SESSION_START, JournalEvent.SESSION_RESUME -> R.string.service_session
            JournalEvent.BREAK_START, JournalEvent.BREAK_RESUME -> R.string.service_break
            else -> null
        }
    }
}