package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.alarm.AlarmScheduler
import com.example.data.local.AlarmEntity
import com.example.data.model.WorldCitiesDirectory
import com.example.data.model.WorldClockDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CurrentClockState(
    val timeFormatted: String = "00:00",
    val secondsFormatted: String = "00",
    val amPm: String = "",
    val dayOfWeek: String = "",
    val fullDate: String = "",
    val timeZoneName: String = "",
    val timeZoneOffset: String = "",
    val rawHour: Int = 0,
    val rawMinute: Int = 0,
    val rawSecond: Int = 0
)

data class NextAlarmInfo(
    val hasAlarm: Boolean = false,
    val formattedTime: String = "",
    val label: String = "",
    val timeRemaining: String = ""
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val dao = app.database.salimDao()
    val settingsRepo = app.settingsRepository

    private val _clockState = MutableStateFlow(CurrentClockState())
    val clockState: StateFlow<CurrentClockState> = _clockState.asStateFlow()

    private val _nextAlarmInfo = MutableStateFlow(NextAlarmInfo())
    val nextAlarmInfo: StateFlow<NextAlarmInfo> = _nextAlarmInfo.asStateFlow()

    // World clock summary for Home Screen
    val worldClockSummary: StateFlow<List<WorldClockDisplay>> = combine(
        dao.getAllWorldClocksFlow(),
        settingsRepo.settingsFlow,
        _clockState
    ) { clocks, settings, _ ->
        clocks.take(4).map { entity ->
            WorldCitiesDirectory.calculateDisplay(
                id = entity.id,
                cityName = entity.cityName,
                countryName = entity.countryName,
                timeZoneId = entity.timeZoneId,
                use24Hour = settings.is24HourFormat
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        startClockTick()
        observeAlarmsForNextAlarm()
    }

    private fun startClockTick() {
        viewModelScope.launch {
            while (isActive) {
                updateTime()
                delay(1000L - (System.currentTimeMillis() % 1000L))
            }
        }
    }

    private fun updateTime() {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val is24Hour = settingsRepo.settingsFlow.value.is24HourFormat

        val timePattern = if (is24Hour) "HH:mm" else "hh:mm"
        val timeFormatted = now.format(DateTimeFormatter.ofPattern(timePattern, Locale.US))
        val secondsFormatted = String.format(Locale.US, "%02d", now.second)
        val amPm = if (is24Hour) "" else now.format(DateTimeFormatter.ofPattern("a", Locale.US))
        val dayOfWeek = now.format(DateTimeFormatter.ofPattern("EEEE", Locale.US))
        val fullDate = now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US))

        val zone = now.zone
        val zoneDisplayName = zone.id.substringAfterLast("/").replace("_", " ")
        val offsetString = now.offset.id

        _clockState.value = CurrentClockState(
            timeFormatted = timeFormatted,
            secondsFormatted = secondsFormatted,
            amPm = amPm,
            dayOfWeek = dayOfWeek,
            fullDate = fullDate,
            timeZoneName = zoneDisplayName,
            timeZoneOffset = "GMT$offsetString",
            rawHour = now.hour,
            rawMinute = now.minute,
            rawSecond = now.second
        )
    }

    private fun observeAlarmsForNextAlarm() {
        viewModelScope.launch {
            dao.getAllAlarmsFlow().collect { alarms ->
                val enabled = alarms.filter { it.isEnabled }
                if (enabled.isEmpty()) {
                    _nextAlarmInfo.value = NextAlarmInfo(hasAlarm = false)
                } else {
                    // Find the one with earliest trigger epoch
                    var earliestAlarm: AlarmEntity? = null
                    var minEpoch = Long.MAX_VALUE

                    for (alarm in enabled) {
                        val epoch = AlarmScheduler.calculateNextTriggerMillis(
                            alarm.hour,
                            alarm.minute,
                            alarm.getRepeatDaysList()
                        )
                        if (epoch < minEpoch) {
                            minEpoch = epoch
                            earliestAlarm = alarm
                        }
                    }

                    if (earliestAlarm != null) {
                        val is24Hour = settingsRepo.settingsFlow.value.is24HourFormat
                        val triggerZoned = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(minEpoch),
                            ZoneId.systemDefault()
                        )
                        val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
                        val timeStr = triggerZoned.format(DateTimeFormatter.ofPattern(pattern, Locale.US))
                        val remaining = AlarmScheduler.getTimeRemainingDescription(
                            earliestAlarm.hour,
                            earliestAlarm.minute,
                            earliestAlarm.getRepeatDaysList()
                        )

                        _nextAlarmInfo.value = NextAlarmInfo(
                            hasAlarm = true,
                            formattedTime = timeStr,
                            label = earliestAlarm.label,
                            timeRemaining = remaining
                        )
                    }
                }
            }
        }
    }
}
