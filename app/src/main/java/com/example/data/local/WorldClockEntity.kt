package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_clocks")
data class WorldClockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val countryName: String,
    val timeZoneId: String,
    val orderIndex: Int = 0
)
