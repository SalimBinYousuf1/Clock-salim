package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val is24HourFormat: Boolean,
    val showSeconds: Boolean,
    val defaultSnoozeMinutes: Int,
    val vibrateEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val themeMode: String // "SYSTEM", "DARK", "LIGHT"
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("salim_clock_settings", Context.MODE_PRIVATE)

    private val isSystem24Hour = DateFormat.is24HourFormat(context)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            is24HourFormat = prefs.getBoolean("is_24_hour", isSystem24Hour),
            showSeconds = prefs.getBoolean("show_seconds", true),
            defaultSnoozeMinutes = prefs.getInt("default_snooze_minutes", 10),
            vibrateEnabled = prefs.getBoolean("vibrate_enabled", true),
            hapticsEnabled = prefs.getBoolean("haptics_enabled", true),
            themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
        )
    }

    fun set24HourFormat(enabled: Boolean) {
        prefs.edit().putBoolean("is_24_hour", enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(is24HourFormat = enabled)
    }

    fun setShowSeconds(show: Boolean) {
        prefs.edit().putBoolean("show_seconds", show).apply()
        _settingsFlow.value = _settingsFlow.value.copy(showSeconds = show)
    }

    fun setDefaultSnoozeMinutes(minutes: Int) {
        prefs.edit().putInt("default_snooze_minutes", minutes).apply()
        _settingsFlow.value = _settingsFlow.value.copy(defaultSnoozeMinutes = minutes)
    }

    fun setVibrateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibrate_enabled", enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(vibrateEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(hapticsEnabled = enabled)
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _settingsFlow.value = _settingsFlow.value.copy(themeMode = mode)
    }
}
