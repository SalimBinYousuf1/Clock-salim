package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "Alarm",
    val daysOfWeek: String = "", // Comma-separated: "1,2,3,4,5" (1=Mon..7=Sun), empty = one-time
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 10,
    val ringtoneName: String = "Default",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getRepeatDaysList(): List<Int> {
        if (daysOfWeek.isBlank()) return emptyList()
        return daysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
    }

    fun isRepeating(): Boolean = getRepeatDaysList().isNotEmpty()

    fun getDaysSummary(): String {
        val days = getRepeatDaysList()
        return when {
            days.isEmpty() -> "Once"
            days.size == 7 -> "Every day"
            days == listOf(1, 2, 3, 4, 5) -> "Weekdays"
            days == listOf(6, 7) -> "Weekends"
            else -> {
                val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                days.map { names[it - 1] }.joinToString(" ")
            }
        }
    }
}
