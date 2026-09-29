package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.SalimDatabase
import com.example.data.repository.SettingsRepository
import com.example.receiver.AlarmReceiver
import com.example.service.AlarmRingtoneService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SalimApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { SalimDatabase.getDatabase(this, applicationScope) }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java) ?: return

            val alarmChannel = NotificationChannel(
                AlarmRingtoneService.CHANNEL_ID,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Salim precision alarm alerts"
                setSound(null, null)
                enableVibration(false)
            }

            val timerChannel = NotificationChannel(
                AlarmReceiver.TIMER_CHANNEL_ID,
                "Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Salim timer completions"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(timerChannel)
        }
    }
}
