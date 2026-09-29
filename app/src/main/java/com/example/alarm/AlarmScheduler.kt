package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.local.AlarmEntity
import com.example.receiver.AlarmReceiver
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    const val EXTRA_ALARM_VIBRATE = "extra_alarm_vibrate"
    const val EXTRA_ALARM_SNOOZE_MINUTES = "extra_alarm_snooze_minutes"

    fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancelAlarm(context, alarm.id)
            return
        }

        val triggerEpochMillis = calculateNextTriggerMillis(alarm.hour, alarm.minute, alarm.getRepeatDaysList())

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_ALARM_VIBRATE, alarm.vibrate)
            putExtra(EXTRA_ALARM_SNOOZE_MINUTES, alarm.snoozeMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show intent for AlarmClockInfo
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt() + 100000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerEpochMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled alarm ${alarm.id} for epoch $triggerEpochMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "Failed setAlarmClock with SecurityException, falling back to setExactAndAllowWhileIdle", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Could not schedule alarm", ex)
            }
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Canceled alarm $alarmId")
    }

    fun scheduleSnooze(context: Context, alarmId: Long, label: String, snoozeMinutes: Int, vibrate: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerEpochMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, "$label (Snoozed)")
            putExtra(EXTRA_ALARM_VIBRATE, vibrate)
            putExtra(EXTRA_ALARM_SNOOZE_MINUTES, snoozeMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt() + 100000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerEpochMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: Exception) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
        }
    }

    fun calculateNextTriggerMillis(hour: Int, minute: Int, repeatDays: List<Int>): Long {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val todayAlarmTime = now.with(LocalTime.of(hour, minute, 0, 0))

        if (repeatDays.isEmpty()) {
            // One-time alarm
            return if (todayAlarmTime.isAfter(now)) {
                todayAlarmTime.toInstant().toEpochMilli()
            } else {
                todayAlarmTime.plusDays(1).toInstant().toEpochMilli()
            }
        }

        // Repeating alarm: check today and next 7 days
        for (dayOffset in 0..7) {
            val candidateDate = todayAlarmTime.plusDays(dayOffset.toLong())
            val candidateDayOfWeek = candidateDate.dayOfWeek.value // 1=Mon..7=Sun
            if (repeatDays.contains(candidateDayOfWeek)) {
                if (candidateDate.isAfter(now)) {
                    return candidateDate.toInstant().toEpochMilli()
                }
            }
        }

        // Fallback
        return todayAlarmTime.plusDays(1).toInstant().toEpochMilli()
    }

    fun getTimeRemainingDescription(hour: Int, minute: Int, repeatDays: List<Int>): String {
        val nextMillis = calculateNextTriggerMillis(hour, minute, repeatDays)
        val diffMillis = nextMillis - System.currentTimeMillis()
        if (diffMillis <= 0) return "Less than a minute"

        val duration = Duration.ofMillis(diffMillis)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60

        return when {
            hours == 0L && minutes == 0L -> "in less than a minute"
            hours == 0L -> "in $minutes min"
            minutes == 0L -> "in $hours hr"
            else -> "in ${hours}h ${minutes}m"
        }
    }
}
