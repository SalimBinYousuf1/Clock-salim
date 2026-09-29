package com.example.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.alarm.AlarmScheduler
import com.example.data.local.AlarmEntity
import com.example.receiver.AlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val dao = app.database.salimDao()

    val alarms: StateFlow<List<AlarmEntity>> = dao.getAllAlarmsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingAlarm = MutableStateFlow<AlarmEntity?>(null)
    val editingAlarm: StateFlow<AlarmEntity?> = _editingAlarm.asStateFlow()

    private val _isEditorOpen = MutableStateFlow(false)
    val isEditorOpen: StateFlow<Boolean> = _isEditorOpen.asStateFlow()

    fun openNewAlarmEditor() {
        val defaultSnooze = app.settingsRepository.settingsFlow.value.defaultSnoozeMinutes
        val defaultVibrate = app.settingsRepository.settingsFlow.value.vibrateEnabled
        _editingAlarm.value = AlarmEntity(
            id = 0,
            hour = 7,
            minute = 0,
            isEnabled = true,
            label = "Alarm",
            daysOfWeek = "",
            vibrate = defaultVibrate,
            snoozeMinutes = defaultSnooze
        )
        _isEditorOpen.value = true
    }

    fun openEditAlarm(alarm: AlarmEntity) {
        _editingAlarm.value = alarm
        _isEditorOpen.value = true
    }

    fun closeEditor() {
        _isEditorOpen.value = false
        _editingAlarm.value = null
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            dao.updateAlarm(updated)
            if (updated.isEnabled) {
                AlarmScheduler.scheduleAlarm(app, updated)
            } else {
                AlarmScheduler.cancelAlarm(app, alarm.id)
            }
        }
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = dao.insertAlarm(alarm)
            val savedAlarm = if (alarm.id == 0L) alarm.copy(id = id) else alarm
            if (savedAlarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(app, savedAlarm)
            } else {
                AlarmScheduler.cancelAlarm(app, savedAlarm.id)
            }
            closeEditor()
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(app, alarm.id)
            dao.deleteAlarm(alarm)
        }
    }

    fun duplicateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val duplicated = alarm.copy(
                id = 0,
                label = "${alarm.label} (Copy)",
                createdAt = System.currentTimeMillis()
            )
            val newId = dao.insertAlarm(duplicated)
            val finalAlarm = duplicated.copy(id = newId)
            if (finalAlarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(app, finalAlarm)
            }
        }
    }

    fun triggerAlarmNow(alarm: AlarmEntity) {
        val intent = Intent(app, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmScheduler.EXTRA_ALARM_VIBRATE, alarm.vibrate)
            putExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE_MINUTES, alarm.snoozeMinutes)
        }
        app.sendBroadcast(intent)
    }
}
