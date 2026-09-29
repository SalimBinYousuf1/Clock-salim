package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.AlarmScheduler
import com.example.data.model.WorldCitiesDirectory
import com.example.viewmodel.StopwatchViewModel
import com.example.viewmodel.TimerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Salim app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim", appName)
    }

    @Test
    fun `stopwatch format centiseconds works accurately`() {
        val formatted = StopwatchViewModel.formatStopwatchTime(65430L)
        // 65.43 seconds -> 01:05.43
        assertEquals("01:05.43", formatted)
    }

    @Test
    fun `timer format remaining time works accurately`() {
        val formatted = TimerViewModel.formatRemainingTime(300)
        assertEquals("05:00", formatted)

        val formattedLong = TimerViewModel.formatRemainingTime(3665)
        assertEquals("01:01:05", formattedLong)
    }

    @Test
    fun `world clock calculation produces valid display`() {
        val display = WorldCitiesDirectory.calculateDisplay(
            id = 1L,
            cityName = "Tokyo",
            countryName = "Japan",
            timeZoneId = "Asia/Tokyo",
            use24Hour = true
        )
        assertNotNull(display)
        assertEquals("Tokyo", display.cityName)
        assertTrue(display.formattedTime.isNotEmpty())
    }

    @Test
    fun `alarm scheduler calculates valid future millis`() {
        val futureMillis = AlarmScheduler.calculateNextTriggerMillis(7, 30, emptyList())
        assertTrue("Next trigger millis should be in the future", futureMillis > System.currentTimeMillis())
    }
}
