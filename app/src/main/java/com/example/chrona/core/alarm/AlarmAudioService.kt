package com.example.chrona.core.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.chrona.core.data.AlarmRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.feature.alarms.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmAudioService : Service() {

    companion object {
        private const val TAG = "AlarmAudioService"
        const val SILENT_RINGING_CHANNEL_ID = "chrona_silent_alarm_ringing_v3"
        private const val NOTIFICATION_ID = 9110

        const val ACTION_START = "com.example.chrona.ACTION_START_ALARM_PLAYBACK"
        const val ACTION_DISMISS = "com.example.chrona.ACTION_DISMISS_ALARM_PLAYBACK"
        const val ACTION_SNOOZE = "com.example.chrona.ACTION_SNOOZE_ALARM_PLAYBACK"

        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"

        fun startAlarm(context: Context, alarmId: String) {
            val intent = Intent(context, AlarmAudioService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AlarmAudioService in foreground", e)
            }
        }

        fun dismissAlarm(context: Context, alarmId: String) {
            val intent = Intent(context, AlarmAudioService::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            context.startService(intent)
        }

        fun snoozeAlarm(context: Context, alarmId: String) {
            val intent = Intent(context, AlarmAudioService::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            context.startService(intent)
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentAlarmId: String? = null
    private var volumeRampJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createSilentNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return START_NOT_STICKY

        when (intent.action) {
            ACTION_START -> {
                if (currentAlarmId == alarmId && mediaPlayer?.isPlaying == true) {
                    Log.d(TAG, "Alarm $alarmId already actively playing, ignoring duplicate start")
                    return START_STICKY
                }
                handleStartAlarm(alarmId)
            }
            ACTION_DISMISS -> {
                handleDismissAlarm(alarmId)
            }
            ACTION_SNOOZE -> {
                handleSnoozeAlarm(alarmId)
            }
        }

        return START_STICKY
    }

    private fun handleStartAlarm(alarmId: String) {
        currentAlarmId = alarmId
        acquireWakeLock()

        val alarm = AlarmRepository.getAlarmById(this, alarmId)
        val alarmTitle = alarm?.label ?: "Chrona Alarm"
        val subtitle = if (alarm != null && alarm.isCrossTimezone) {
            "${alarm.formattedTargetTime()} ${alarm.targetAmPm()} ${alarm.targetCityName} Time"
        } else {
            "Time to wake up"
        }

        val notification = buildOngoingAlarmNotification(alarmId, alarmTitle, subtitle, alarm?.snoozeDurationMinutes ?: 10)
        startForeground(NOTIFICATION_ID, notification)

        // Broadcast or set in repository for UI visibility
        AlarmRepository.setRingingAlarm(alarm)

        // Initialize single authoritative audio player
        val rampSeconds = if (alarm?.gradualVolumeRamp == true) 30 else 0
        startLoopingAudio(alarm?.soundUri, rampSeconds)

        // Initialize vibration if enabled
        val globalVibrate = PreferencesRepository.preferences.value.vibrationEnabled
        if ((alarm?.vibrate == true) && globalVibrate) {
            startVibrating()
        }
    }

    private fun handleDismissAlarm(alarmId: String) {
        Log.d(TAG, "Dismissing alarm $alarmId")
        stopAudioAndVibration()
        AlarmRepository.setRingingAlarm(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleSnoozeAlarm(alarmId: String) {
        Log.d(TAG, "Snoozing alarm $alarmId")
        stopAudioAndVibration()
        AlarmRepository.setRingingAlarm(null)
        AlarmRepository.snoozeAlarm(this, alarmId)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLoopingAudio(soundUriString: String?, gradualRampSeconds: Int) {
        stopAudioOnly()

        if (soundUriString == "silent") {
            Log.d(TAG, "Alarm is configured as silent; audio playback skipped")
            return
        }

        try {
            requestAudioFocus()

            val soundUri = if (!soundUriString.isNullOrBlank()) {
                Uri.parse(soundUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            val player = MediaPlayer().apply {
                setDataSource(applicationContext, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
            }

            player.prepare()

            val initialVolume = if (gradualRampSeconds > 0) 0.05f else 1.0f
            player.setVolume(initialVolume, initialVolume)
            player.start()
            mediaPlayer = player

            if (gradualRampSeconds > 0) {
                startGradualVolumeRamp(gradualRampSeconds)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaPlayer for alarm sound, falling back to default", e)
            tryFallbackDefaultPlayer()
        }
    }

    private fun tryFallbackDefaultPlayer() {
        try {
            val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val player = MediaPlayer().apply {
                setDataSource(applicationContext, fallbackUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
            }
            player.prepare()
            player.setVolume(1.0f, 1.0f)
            player.start()
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Fallback player failed too", e)
        }
    }

    private fun startGradualVolumeRamp(rampSeconds: Int) {
        volumeRampJob?.cancel()
        volumeRampJob = serviceScope.launch {
            val steps = 30
            val intervalMs = (rampSeconds * 1000L) / steps
            for (step in 1..steps) {
                delay(intervalMs)
                val targetVol = (step.toFloat() / steps).coerceIn(0.05f, 1.0f)
                try {
                    mediaPlayer?.setVolume(targetVol, targetVol)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun startVibrating() {
        try {
            val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (v != null && v.hasVibrator()) {
                val pattern = longArrayOf(0, 600, 400, 600, 400, 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(pattern, 0)) // loop at index 0
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(pattern, 0)
                }
                vibrator = v
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibrating", e)
        }
    }

    private fun requestAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { /* maintain alarm priority */ }
                .build()
            audioFocusRequest = req
            audioManager.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
        }
    }

    private fun abandonAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "chrona:AlarmAudioServiceWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes safety timeout
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock", e)
        }
    }

    private fun stopAudioOnly() {
        volumeRampJob?.cancel()
        volumeRampJob = null
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        }
        abandonAudioFocus()
    }

    private fun stopAudioAndVibration() {
        stopAudioOnly()
        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibrator", e)
        }
        releaseWakeLock()
    }

    private fun buildOngoingAlarmNotification(
        alarmId: String,
        title: String,
        subtitle: String,
        snoozeMinutes: Int
    ): Notification {
        // Dedicated standalone ringing activity
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmAudioService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            alarmId.hashCode() + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmAudioService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            alarmId.hashCode() + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SILENT_RINGING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSound(null) // STRICTLY SILENT NOTIFICATION: AlarmAudioService MediaPlayer is the sole owner
            .setVibrate(null)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze · ${snoozeMinutes}m", snoozePendingIntent)
            .build()
    }

    private fun createSilentNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                SILENT_RINGING_CHANNEL_ID,
                "Chrona Alarm Ringing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active ringing alarms for Chrona"
                // Channel sound and vibration must be null so Android doesn't play a second sound!
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopAudioAndVibration()
        super.onDestroy()
    }
}
