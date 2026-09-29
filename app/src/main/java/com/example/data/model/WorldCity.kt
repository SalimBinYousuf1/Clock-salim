package com.example.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class WorldCity(
    val cityName: String,
    val countryName: String,
    val timeZoneId: String
)

data class WorldClockDisplay(
    val id: Long,
    val cityName: String,
    val countryName: String,
    val timeZoneId: String,
    val formattedTime: String,
    val amPm: String,
    val formattedDate: String,
    val timeDifference: String,
    val dayRelative: String,
    val isAhead: Boolean,
    val rawHour: Int,
    val rawMinute: Int,
    val rawSecond: Int
)

object WorldCitiesDirectory {
    val CITIES = listOf(
        WorldCity("Amsterdam", "Netherlands", "Europe/Amsterdam"),
        WorldCity("Athens", "Greece", "Europe/Athens"),
        WorldCity("Auckland", "New Zealand", "Pacific/Auckland"),
        WorldCity("Bangkok", "Thailand", "Asia/Bangkok"),
        WorldCity("Barcelona", "Spain", "Europe/Madrid"),
        WorldCity("Beijing", "China", "Asia/Shanghai"),
        WorldCity("Berlin", "Germany", "Europe/Berlin"),
        WorldCity("Bogota", "Colombia", "America/Bogota"),
        WorldCity("Boston", "United States", "America/New_York"),
        WorldCity("Brussels", "Belgium", "Europe/Brussels"),
        WorldCity("Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires"),
        WorldCity("Cairo", "Egypt", "Africa/Cairo"),
        WorldCity("Cape Town", "South Africa", "Africa/Johannesburg"),
        WorldCity("Chicago", "United States", "America/Chicago"),
        WorldCity("Copenhagen", "Denmark", "Europe/Copenhagen"),
        WorldCity("Dallas", "United States", "America/Chicago"),
        WorldCity("Delhi", "India", "Asia/Kolkata"),
        WorldCity("Denver", "United States", "America/Denver"),
        WorldCity("Doha", "Qatar", "Asia/Qatar"),
        WorldCity("Dubai", "United Arab Emirates", "Asia/Dubai"),
        WorldCity("Dublin", "Ireland", "Europe/Dublin"),
        WorldCity("Frankfurt", "Germany", "Europe/Berlin"),
        WorldCity("Geneva", "Switzerland", "Europe/Zurich"),
        WorldCity("Helsinki", "Finland", "Europe/Helsinki"),
        WorldCity("Hong Kong", "Hong Kong", "Asia/Hong_Kong"),
        WorldCity("Honolulu", "United States", "Pacific/Honolulu"),
        WorldCity("Houston", "United States", "America/Chicago"),
        WorldCity("Istanbul", "Turkey", "Europe/Istanbul"),
        WorldCity("Jakarta", "Indonesia", "Asia/Jakarta"),
        WorldCity("Johannesburg", "South Africa", "Africa/Johannesburg"),
        WorldCity("Kuala Lumpur", "Malaysia", "Asia/Kuala_Lumpur"),
        WorldCity("Kyiv", "Ukraine", "Europe/Kyiv"),
        WorldCity("Lagos", "Nigeria", "Africa/Lagos"),
        WorldCity("Lima", "Peru", "America/Lima"),
        WorldCity("Lisbon", "Portugal", "Europe/Lisbon"),
        WorldCity("London", "United Kingdom", "Europe/London"),
        WorldCity("Los Angeles", "United States", "America/Los_Angeles"),
        WorldCity("Madrid", "Spain", "Europe/Madrid"),
        WorldCity("Manila", "Philippines", "Asia/Manila"),
        WorldCity("Melbourne", "Australia", "Australia/Melbourne"),
        WorldCity("Mexico City", "Mexico", "America/Mexico_City"),
        WorldCity("Miami", "United States", "America/New_York"),
        WorldCity("Milan", "Italy", "Europe/Rome"),
        WorldCity("Montreal", "Canada", "America/Toronto"),
        WorldCity("Moscow", "Russia", "Europe/Moscow"),
        WorldCity("Mumbai", "India", "Asia/Kolkata"),
        WorldCity("Munich", "Germany", "Europe/Berlin"),
        WorldCity("Nairobi", "Kenya", "Africa/Nairobi"),
        WorldCity("New York", "United States", "America/New_York"),
        WorldCity("Oslo", "Norway", "Europe/Oslo"),
        WorldCity("Paris", "France", "Europe/Paris"),
        WorldCity("Perth", "Australia", "Australia/Perth"),
        WorldCity("Phoenix", "United States", "America/Phoenix"),
        WorldCity("Prague", "Czech Republic", "Europe/Prague"),
        WorldCity("Reykjavik", "Iceland", "Atlantic/Reykjavik"),
        WorldCity("Rio de Janeiro", "Brazil", "America/Sao_Paulo"),
        WorldCity("Riyadh", "Saudi Arabia", "Asia/Riyadh"),
        WorldCity("Rome", "Italy", "Europe/Rome"),
        WorldCity("San Francisco", "United States", "America/Los_Angeles"),
        WorldCity("Santiago", "Chile", "America/Santiago"),
        WorldCity("Sao Paulo", "Brazil", "America/Sao_Paulo"),
        WorldCity("Seattle", "United States", "America/Los_Angeles"),
        WorldCity("Seoul", "South Korea", "Asia/Seoul"),
        WorldCity("Shanghai", "China", "Asia/Shanghai"),
        WorldCity("Singapore", "Singapore", "Asia/Singapore"),
        WorldCity("Stockholm", "Sweden", "Europe/Stockholm"),
        WorldCity("Sydney", "Australia", "Australia/Sydney"),
        WorldCity("Taipei", "Taiwan", "Asia/Taipei"),
        WorldCity("Tokyo", "Japan", "Asia/Tokyo"),
        WorldCity("Toronto", "Canada", "America/Toronto"),
        WorldCity("Vancouver", "Canada", "America/Vancouver"),
        WorldCity("Vienna", "Austria", "Europe/Vienna"),
        WorldCity("Warsaw", "Poland", "Europe/Warsaw"),
        WorldCity("Zurich", "Switzerland", "Europe/Zurich")
    )

    fun calculateDisplay(
        id: Long,
        cityName: String,
        countryName: String,
        timeZoneId: String,
        use24Hour: Boolean
    ): WorldClockDisplay {
        val now = Instant.now()
        val targetZone = try {
            ZoneId.of(timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }
        val localZone = ZoneId.systemDefault()

        val targetZoned = ZonedDateTime.ofInstant(now, targetZone)
        val localZoned = ZonedDateTime.ofInstant(now, localZone)

        val timePattern = if (use24Hour) "HH:mm" else "hh:mm"
        val formattedTime = targetZoned.format(DateTimeFormatter.ofPattern(timePattern, Locale.US))
        val amPm = if (use24Hour) "" else targetZoned.format(DateTimeFormatter.ofPattern("a", Locale.US))

        val formattedDate = targetZoned.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US))

        // Difference calculation
        val targetOffsetSec = targetZoned.offset.totalSeconds
        val localOffsetSec = localZoned.offset.totalSeconds
        val diffSeconds = targetOffsetSec - localOffsetSec
        val diffHours = diffSeconds / 3600.0

        val timeDifference = when {
            diffSeconds == 0 -> "Same time"
            diffHours % 1.0 == 0.0 -> {
                val h = diffHours.toInt()
                if (h > 0) "+${h}HRS" else "${h}HRS"
            }
            else -> {
                val sign = if (diffHours > 0) "+" else ""
                String.format(Locale.US, "%s%.1fHRS", sign, diffHours)
            }
        }

        val targetLocalDate = targetZoned.toLocalDate()
        val localLocalDate = localZoned.toLocalDate()
        val dayDiff = ChronoUnit.DAYS.between(localLocalDate, targetLocalDate)

        val dayRelative = when (dayDiff) {
            -1L -> "Yesterday"
            1L -> "Tomorrow"
            else -> "Today"
        }

        return WorldClockDisplay(
            id = id,
            cityName = cityName,
            countryName = countryName,
            timeZoneId = timeZoneId,
            formattedTime = formattedTime,
            amPm = amPm,
            formattedDate = formattedDate,
            timeDifference = timeDifference,
            dayRelative = dayRelative,
            isAhead = diffSeconds > 0,
            rawHour = targetZoned.hour,
            rawMinute = targetZoned.minute,
            rawSecond = targetZoned.second
        )
    }
}
