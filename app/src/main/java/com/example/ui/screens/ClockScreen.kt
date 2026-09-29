package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SalimAnalogClock
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimOrange
import com.example.viewmodel.ClockViewModel

@Composable
fun ClockScreen(
    viewModel: ClockViewModel,
    onNavigateToAlarms: () -> Unit,
    onNavigateToWorldClock: () -> Unit
) {
    val clockState by viewModel.clockState.collectAsState()
    val nextAlarm by viewModel.nextAlarmInfo.collectAsState()
    val worldSummary by viewModel.worldClockSummary.collectAsState()
    val settings by viewModel.settingsRepo.settingsFlow.collectAsState()

    var showAnalogClock by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Switcher: Digital vs Minimalist Analog
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (!showAnalogClock) SalimBlue else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showAnalogClock = false }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("clock_digital_toggle")
            ) {
                Text(
                    text = "Digital",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!showAnalogClock) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (showAnalogClock) SalimBlue else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showAnalogClock = true }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("clock_analog_toggle")
            ) {
                Text(
                    text = "Analog",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (showAnalogClock) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (showAnalogClock) {
            SalimAnalogClock(
                hour = clockState.rawHour,
                minute = clockState.rawMinute,
                second = clockState.rawSecond,
                showSeconds = settings.showSeconds,
                size = 240.dp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Dominant Focal Point: Main Digital Time
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.testTag("current_time_display")
        ) {
            Text(
                text = clockState.timeFormatted,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (showAnalogClock) 52.sp else 74.sp
                ),
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (settings.showSeconds) {
                Text(
                    text = ":${clockState.secondsFormatted}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = if (showAnalogClock) 28.sp else 38.sp
                    ),
                    fontWeight = FontWeight.ExtraLight,
                    color = SalimOrange,
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }

            if (clockState.amPm.isNotEmpty()) {
                Text(
                    text = clockState.amPm,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Date and Day
        Text(
            text = "${clockState.dayOfWeek}, ${clockState.fullDate}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Local Timezone Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${clockState.timeZoneName} • ${clockState.timeZoneOffset}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Next Upcoming Alarm Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToAlarms() }
                .testTag("next_alarm_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (nextAlarm.hasAlarm) SalimBlue.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = "Alarm icon",
                            tint = if (nextAlarm.hasAlarm) SalimBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (nextAlarm.hasAlarm) "Next Alarm" else "No Alarms Scheduled",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (nextAlarm.hasAlarm) {
                            Text(
                                text = "${nextAlarm.formattedTime} • ${nextAlarm.label}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Tap to set an alarm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (nextAlarm.hasAlarm) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SalimBlue.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = nextAlarm.timeRemaining,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SalimBlue
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // World Clocks Quick Glance
        if (worldSummary.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORLD CLOCK",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelMedium,
                    color = SalimBlue,
                    modifier = Modifier
                        .clickable { onNavigateToWorldClock() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(worldSummary, key = { it.id }) { city ->
                    Card(
                        modifier = Modifier
                            .width(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToWorldClock() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = city.cityName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${city.timeDifference}, ${city.dayRelative}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = city.formattedTime,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
