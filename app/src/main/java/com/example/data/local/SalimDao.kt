package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalimDao {
    // Alarms
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarmsFlow(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean)

    // World Clock
    @Query("SELECT * FROM world_clocks ORDER BY orderIndex ASC, id ASC")
    fun getAllWorldClocksFlow(): Flow<List<WorldClockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorldClock(clock: WorldClockEntity): Long

    @Delete
    suspend fun deleteWorldClock(clock: WorldClockEntity)

    @Query("DELETE FROM world_clocks WHERE id = :id")
    suspend fun deleteWorldClockById(id: Long)

    @Query("SELECT COUNT(*) FROM world_clocks")
    suspend fun getWorldClockCount(): Int

    // Timer Presets
    @Query("SELECT * FROM timer_presets ORDER BY durationSeconds ASC")
    fun getAllTimerPresetsFlow(): Flow<List<TimerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimerPreset(preset: TimerPresetEntity): Long

    @Delete
    suspend fun deleteTimerPreset(preset: TimerPresetEntity)

    @Query("DELETE FROM timer_presets WHERE id = :id")
    suspend fun deleteTimerPresetById(id: Long)

    @Query("SELECT COUNT(*) FROM timer_presets")
    suspend fun getTimerPresetCount(): Int
}
