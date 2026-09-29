package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.alarm.AlarmScheduler
import com.example.data.local.SalimDatabase
import com.example.service.AlarmRingtoneService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_TRIGGER_ALARM = "com.example.salim.ACTION_TRIGGER_ALARM"
        const val ACTION_SNOOZE = "com.example.salim.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.example.salim.ACTION_DISMISS"
        const val ACTION_TIMER_EXPIRED = "com.example.salim.ACTION_TIMER_EXPIRED"

        const val TIMER_CHANNEL_ID = "salim_timer_alerts"
        const val TIMER_NOTIFICATION_ID = 3001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Received broadcast action: $action")

        val scope = CoroutineScope(Dispatchers.IO)

        when (action) {
            ACTION_TRIGGER_ALARM -> {
                val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
                val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Alarm"
                val vibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_ALARM_VIBRATE, true)
                val snoozeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE_MINUTES, 10)

                // Start AlarmRingtoneService
                val serviceIntent = Intent(context, AlarmRingtoneService::class.java).apply {
                    this.action = AlarmRingtoneService.ACTION_START_RINGING
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
                    putExtra(AlarmScheduler.EXTRA_ALARM_VIBRATE, vibrate)
                    putExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE_MINUTES, snoozeMinutes)
                }

                ContextCompat.startForegroundService(context, serviceIntent)

                // Update database: if one-time alarm, disable it. If repeating, schedule next occurrence.
                if (alarmId > 0) {
                    scope.launch {
                        try {
                            val db = SalimDatabase.getDatabase(context, scope)
                            val alarm = db.salimDao().getAlarmById(alarmId)
                            if (alarm != null) {
                                if (alarm.isRepeating()) {
                                    // Reschedule for next day in cycle
                                    AlarmScheduler.scheduleAlarm(context, alarm)
                                } else {
                                    // One-time alarm fired -> disable
                                    db.salimDao().setAlarmEnabled(alarmId, false)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating alarm post-trigger", e)
                        }
                    }
                }
            }

            ACTION_SNOOZE -> {
                AlarmRingtoneService.stopRinging(context)

                val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
                val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Alarm"
                val snoozeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE_MINUTES, 10)
                val vibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_ALARM_VIBRATE, true)

                AlarmScheduler.scheduleSnooze(context, alarmId, label, snoozeMinutes, vibrate)
            }

            ACTION_DISMISS -> {
                AlarmRingtoneService.stopRinging(context)
            }

            ACTION_TIMER_EXPIRED -> {
                val timerName = intent.getStringExtra("EXTRA_TIMER_NAME") ?: "Timer"
                showTimerCompletedNotification(context, timerName)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                // Device rebooted or time changed -> reschedule all enabled alarms
                scope.launch {
                    try {
                        val db = SalimDatabase.getDatabase(context, scope)
                        val enabledAlarms = db.salimDao().getEnabledAlarms()
                        for (alarm in enabledAlarms) {
                            AlarmScheduler.scheduleAlarm(context, alarm)
                        }
                        Log.d(TAG, "Rescheduled ${enabledAlarms.size} alarms after system event $action")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error rescheduling alarms on boot/time change", e)
                    }
                }
            }
        }
    }

    private fun showTimerCompletedNotification(context: Context, timerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Timer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when your timer finishes"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "timer")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            TIMER_NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Timer Finished")
            .setContentText("$timerName has completed.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 300, 400, 300, 600))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(TIMER_NOTIFICATION_ID, notification)
    }
}
