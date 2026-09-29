package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AlarmEntity::class,
        WorldClockEntity::class,
        TimerPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SalimDatabase : RoomDatabase() {
    abstract fun salimDao(): SalimDao

    companion object {
        @Volatile
        private var INSTANCE: SalimDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SalimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalimDatabase::class.java,
                    "salim_clock_database"
                )
                    .addCallback(SalimDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SalimDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.salimDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: SalimDao) {
                // Populate default starter alarms
                dao.insertAlarm(
                    AlarmEntity(
                        hour = 7,
                        minute = 0,
                        isEnabled = true,
                        label = "Morning Routine",
                        daysOfWeek = "1,2,3,4,5", // Mon-Fri
                        vibrate = true,
                        snoozeMinutes = 10
                    )
                )
                dao.insertAlarm(
                    AlarmEntity(
                        hour = 8,
                        minute = 30,
                        isEnabled = false,
                        label = "Weekend Wakeup",
                        daysOfWeek = "6,7", // Sat-Sun
                        vibrate = true,
                        snoozeMinutes = 15
                    )
                )

                // Populate default world clocks
                dao.insertWorldClock(
                    WorldClockEntity(
                        cityName = "London",
                        countryName = "United Kingdom",
                        timeZoneId = "Europe/London",
                        orderIndex = 0
                    )
                )
                dao.insertWorldClock(
                    WorldClockEntity(
                        cityName = "New York",
                        countryName = "United States",
                        timeZoneId = "America/New_York",
                        orderIndex = 1
                    )
                )
                dao.insertWorldClock(
                    WorldClockEntity(
                        cityName = "Tokyo",
                        countryName = "Japan",
                        timeZoneId = "Asia/Tokyo",
                        orderIndex = 2
                    )
                )
                dao.insertWorldClock(
                    WorldClockEntity(
                        cityName = "Dubai",
                        countryName = "United Arab Emirates",
                        timeZoneId = "Asia/Dubai",
                        orderIndex = 3
                    )
                )

                // Populate default timer presets
                dao.insertTimerPreset(TimerPresetEntity(name = "Tea", durationSeconds = 60))
                dao.insertTimerPreset(TimerPresetEntity(name = "Egg Boil", durationSeconds = 180))
                dao.insertTimerPreset(TimerPresetEntity(name = "Quick Rest", durationSeconds = 300))
                dao.insertTimerPreset(TimerPresetEntity(name = "Power Nap", durationSeconds = 900))
                dao.insertTimerPreset(TimerPresetEntity(name = "Focus Sprint", durationSeconds = 1500))
            }
        }
    }
}
