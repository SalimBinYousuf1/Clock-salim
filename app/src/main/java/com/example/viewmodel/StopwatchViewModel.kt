package com.example.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class StopwatchState {
    IDLE,
    RUNNING,
    PAUSED
}

data class LapRecord(
    val lapIndex: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long,
    val isFastest: Boolean = false,
    val isSlowest: Boolean = false
)

class StopwatchViewModel(application: Application) : AndroidViewModel(application) {

    private val _stopwatchState = MutableStateFlow(StopwatchState.IDLE)
    val stopwatchState: StateFlow<StopwatchState> = _stopwatchState.asStateFlow()

    private val _totalElapsedMillis = MutableStateFlow(0L)
    val totalElapsedMillis: StateFlow<Long> = _totalElapsedMillis.asStateFlow()

    private val _currentLapElapsedMillis = MutableStateFlow(0L)
    val currentLapElapsedMillis: StateFlow<Long> = _currentLapElapsedMillis.asStateFlow()

    private val _laps = MutableStateFlow<List<LapRecord>>(emptyList())
    val laps: StateFlow<List<LapRecord>> = _laps.asStateFlow()

    private var tickerJob: Job? = null

    private var baseStartTime: Long = 0L
    private var accumulatedElapsed: Long = 0L

    private var lapStartTime: Long = 0L
    private var accumulatedLapElapsed: Long = 0L

    fun start() {
        if (_stopwatchState.value == StopwatchState.RUNNING) return

        val now = SystemClock.elapsedRealtime()
        baseStartTime = now
        lapStartTime = now
        _stopwatchState.value = StopwatchState.RUNNING

        startTicker()
    }

    fun pause() {
        if (_stopwatchState.value != StopwatchState.RUNNING) return

        val now = SystemClock.elapsedRealtime()
        accumulatedElapsed += (now - baseStartTime)
        accumulatedLapElapsed += (now - lapStartTime)

        _totalElapsedMillis.value = accumulatedElapsed
        _currentLapElapsedMillis.value = accumulatedLapElapsed
        _stopwatchState.value = StopwatchState.PAUSED

        tickerJob?.cancel()
        tickerJob = null
    }

    fun resume() {
        if (_stopwatchState.value != StopwatchState.PAUSED) return

        val now = SystemClock.elapsedRealtime()
        baseStartTime = now
        lapStartTime = now
        _stopwatchState.value = StopwatchState.RUNNING

        startTicker()
    }

    fun reset() {
        tickerJob?.cancel()
        tickerJob = null

        baseStartTime = 0L
        accumulatedElapsed = 0L
        lapStartTime = 0L
        accumulatedLapElapsed = 0L

        _totalElapsedMillis.value = 0L
        _currentLapElapsedMillis.value = 0L
        _laps.value = emptyList()
        _stopwatchState.value = StopwatchState.IDLE
    }

    fun recordLap() {
        if (_stopwatchState.value != StopwatchState.RUNNING) return

        val now = SystemClock.elapsedRealtime()
        val lapDuration = accumulatedLapElapsed + (now - lapStartTime)
        val totalDuration = accumulatedElapsed + (now - baseStartTime)

        // Reset lap timer
        lapStartTime = now
        accumulatedLapElapsed = 0L
        _currentLapElapsedMillis.value = 0L

        val currentLaps = _laps.value.toMutableList()
        val nextIndex = currentLaps.size + 1

        val newRecord = LapRecord(
            lapIndex = nextIndex,
            lapTimeMillis = lapDuration,
            totalTimeMillis = totalDuration
        )
        currentLaps.add(0, newRecord) // Add to top for recent laps first

        // Update fastest and slowest if 2 or more laps
        if (currentLaps.size >= 2) {
            val minLapTime = currentLaps.minOf { it.lapTimeMillis }
            val maxLapTime = currentLaps.maxOf { it.lapTimeMillis }

            _laps.value = currentLaps.map { lap ->
                lap.copy(
                    isFastest = lap.lapTimeMillis == minLapTime,
                    isSlowest = lap.lapTimeMillis == maxLapTime && minLapTime != maxLapTime
                )
            }
        } else {
            _laps.value = currentLaps
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                _totalElapsedMillis.value = accumulatedElapsed + (now - baseStartTime)
                _currentLapElapsedMillis.value = accumulatedLapElapsed + (now - lapStartTime)
                delay(30L) // ~33fps for smooth centisecond ticking without burning CPU
            }
        }
    }

    companion object {
        fun formatStopwatchTime(totalMillis: Long): String {
            val centiseconds = (totalMillis % 1000L) / 10L
            val totalSeconds = totalMillis / 1000L
            val seconds = totalSeconds % 60L
            val minutes = (totalSeconds / 60L) % 60L
            val hours = totalSeconds / 3600L

            return if (hours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds)
            } else {
                String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centiseconds)
            }
        }
    }
}
