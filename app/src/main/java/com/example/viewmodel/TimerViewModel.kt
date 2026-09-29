package com.example.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.TimerPresetEntity
import com.example.receiver.AlarmReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val dao = app.database.salimDao()

    val presets: StateFlow<List<TimerPresetEntity>> = dao.getAllTimerPresetsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Configured inputs
    private val _selectedHours = MutableStateFlow(0)
    val selectedHours: StateFlow<Int> = _selectedHours.asStateFlow()

    private val _selectedMinutes = MutableStateFlow(5)
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    private val _selectedSeconds = MutableStateFlow(0)
    val selectedSeconds: StateFlow<Int> = _selectedSeconds.asStateFlow()

    // Timer runtime state
    private val _timerStatus = MutableStateFlow(TimerStatus.IDLE)
    val timerStatus: StateFlow<TimerStatus> = _timerStatus.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(300)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(300)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _timerLabel = MutableStateFlow("Timer")
    val timerLabel: StateFlow<String> = _timerLabel.asStateFlow()

    private var countdownJob: Job? = null
    private var targetEpochTimeMillis: Long = 0L

    fun setHours(h: Int) {
        if (_timerStatus.value == TimerStatus.IDLE) {
            _selectedHours.value = h.coerceIn(0, 99)
            updateTotalDuration()
        }
    }

    fun setMinutes(m: Int) {
        if (_timerStatus.value == TimerStatus.IDLE) {
            _selectedMinutes.value = m.coerceIn(0, 59)
            updateTotalDuration()
        }
    }

    fun setSeconds(s: Int) {
        if (_timerStatus.value == TimerStatus.IDLE) {
            _selectedSeconds.value = s.coerceIn(0, 59)
            updateTotalDuration()
        }
    }

    private fun updateTotalDuration() {
        val total = (_selectedHours.value * 3600) + (_selectedMinutes.value * 60) + _selectedSeconds.value
        _totalDurationSeconds.value = total
        _remainingSeconds.value = total
    }

    fun startPreset(preset: TimerPresetEntity) {
        val dur = preset.durationSeconds
        _totalDurationSeconds.value = dur
        _remainingSeconds.value = dur
        _timerLabel.value = preset.name

        val h = dur / 3600
        val m = (dur % 3600) / 60
        val s = dur % 60
        _selectedHours.value = h
        _selectedMinutes.value = m
        _selectedSeconds.value = s

        startTimer()
    }

    fun startTimer() {
        val dur = _remainingSeconds.value
        if (dur <= 0) {
            updateTotalDuration()
            if (_remainingSeconds.value <= 0) return
        }

        val remainingSec = _remainingSeconds.value
        targetEpochTimeMillis = SystemClock.elapsedRealtime() + (remainingSec * 1000L)
        _timerStatus.value = TimerStatus.RUNNING

        scheduleBackgroundAlarm(remainingSec)
        startCountdownTicker()
    }

    fun pauseTimer() {
        if (_timerStatus.value != TimerStatus.RUNNING) return

        cancelBackgroundAlarm()
        countdownJob?.cancel()
        countdownJob = null

        val remainingMillis = targetEpochTimeMillis - SystemClock.elapsedRealtime()
        _remainingSeconds.value = (remainingMillis / 1000L).coerceAtLeast(0L).toInt()
        _timerStatus.value = TimerStatus.PAUSED
    }

    fun resumeTimer() {
        if (_timerStatus.value != TimerStatus.PAUSED) return
        startTimer()
    }

    fun cancelOrReset() {
        cancelBackgroundAlarm()
        countdownJob?.cancel()
        countdownJob = null

        updateTotalDuration()
        _timerStatus.value = TimerStatus.IDLE
    }

    fun addPreset(name: String, durationSec: Int) {
        viewModelScope.launch {
            dao.insertTimerPreset(TimerPresetEntity(name = name, durationSeconds = durationSec))
        }
    }

    fun deletePreset(preset: TimerPresetEntity) {
        viewModelScope.launch {
            dao.deleteTimerPreset(preset)
        }
    }

    private fun startCountdownTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                val diffMillis = targetEpochTimeMillis - now
                if (diffMillis <= 0) {
                    _remainingSeconds.value = 0
                    _timerStatus.value = TimerStatus.COMPLETED
                    triggerCompletion()
                    break
                } else {
                    _remainingSeconds.value = (diffMillis / 1000L).toInt() + 1
                }
                delay(200L)
            }
        }
    }

    private fun triggerCompletion() {
        cancelBackgroundAlarm()
        // Trigger vibration
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 300, 200, 300, 200, 500), -1)
            }
        } catch (_: Exception) {}
    }

    private fun scheduleBackgroundAlarm(secondsRemaining: Int) {
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAtMillis = System.currentTimeMillis() + (secondsRemaining * 1000L)

        val intent = Intent(app, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TIMER_EXPIRED
            putExtra("EXTRA_TIMER_NAME", _timerLabel.value)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            app,
            AlarmReceiver.TIMER_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (_: Exception) {}
    }

    private fun cancelBackgroundAlarm() {
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(app, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            app,
            AlarmReceiver.TIMER_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        fun formatRemainingTime(seconds: Int): String {
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            return if (h > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
            } else {
                String.format(Locale.US, "%02d:%02d", m, s)
            }
        }
    }
}
