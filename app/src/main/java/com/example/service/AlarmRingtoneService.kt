package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.alarm.AlarmScheduler
import com.example.receiver.AlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveRingingAlarm(
    val alarmId: Long,
    val label: String,
    val snoozeMinutes: Int
)

class AlarmRingtoneService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoStopRunnable = Runnable { stopAlarm() }

    companion object {
        private const val TAG = "AlarmRingtoneService"
        const val CHANNEL_ID = "salim_active_alarms"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_RINGING = "com.example.salim.ACTION_START_RINGING"
        const val ACTION_STOP_RINGING = "com.example.salim.ACTION_STOP_RINGING"

        private val _currentRingingAlarm = MutableStateFlow<ActiveRingingAlarm?>(null)
        val currentRingingAlarm: StateFlow<ActiveRingingAlarm?> = _currentRingingAlarm.asStateFlow()

        fun isAlarmCurrentlyRinging(): Boolean = _currentRingingAlarm.value != null

        fun stopRinging(context: Context) {
            val stopIntent = Intent(context, AlarmRingtoneService::class.java).apply {
                action = ACTION_STOP_RINGING
            }
            context.startService(stopIntent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        when (intent.action) {
            ACTION_STOP_RINGING -> {
                stopAlarm()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_RINGING -> {
                val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
                val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Alarm"
                val shouldVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_ALARM_VIBRATE, true)
                val snoozeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE_MINUTES, 10)

                _currentRingingAlarm.value = ActiveRingingAlarm(alarmId, label, snoozeMinutes)

                val notification = buildForegroundNotification(alarmId, label, snoozeMinutes, shouldVibrate)
                startForeground(NOTIFICATION_ID, notification)

                startMediaPlayback()
                if (shouldVibrate) {
                    startVibration()
                }

                // Auto stop after 10 minutes to save battery
                handler.removeCallbacks(autoStopRunnable)
                handler.postDelayed(autoStopRunnable, 10 * 60 * 1000L)

                return START_STICKY
            }
        }

        return START_NOT_STICKY
    }

    private fun startMediaPlayback() {
        try {
            var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing alarm media", e)
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 500, 400, 500, 400, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration", e)
        }
    }

    private fun buildForegroundNotification(
        alarmId: Long,
        label: String,
        snoozeMinutes: Int,
        vibrate: Boolean
    ): Notification {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_OPEN_RINGING_ALARM", true)
            putExtra("EXTRA_ALARM_ID", alarmId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId.toInt() + 10,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE_MINUTES, snoozeMinutes)
            putExtra(AlarmScheduler.EXTRA_ALARM_VIBRATE, vibrate)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId.toInt() + 20,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm: $label")
            .setContentText("Tap to open or dismiss below")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, "Dismiss", dismissPendingIntent)
            .addAction(0, "Snooze (${snoozeMinutes}m)", snoozePendingIntent)
            .build()
    }

    private fun stopAlarm() {
        handler.removeCallbacks(autoStopRunnable)
        _currentRingingAlarm.value = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        vibrator = null
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Salim precision alarm alerts"
                setSound(null, null) // Audio is handled by MediaPlayer
                enableVibration(false) // Handled by Vibrator
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }
}
