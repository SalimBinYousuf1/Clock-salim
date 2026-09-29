package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.data.local.AlarmEntity
import com.example.ui.theme.SalimBlue
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditorSheet(
    sheetState: SheetState,
    alarm: AlarmEntity,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit,
    onDelete: ((AlarmEntity) -> Unit)? = null
) {
    var rawHour by remember { mutableIntStateOf(alarm.hour) }
    var rawMinute by remember { mutableIntStateOf(alarm.minute) }
    var label by remember { mutableStateOf(alarm.label) }
    var vibrate by remember { mutableStateOf(alarm.vibrate) }
    var snoozeMinutes by remember { mutableIntStateOf(alarm.snoozeMinutes) }
    var ringtoneName by remember { mutableStateOf(alarm.ringtoneName) }

    val selectedDays = remember {
        mutableStateListOf<Int>().apply { addAll(alarm.getRepeatDaysList()) }
    }

    val displayHour = if (is24Hour) {
        rawHour
    } else {
        when {
            rawHour == 0 -> 12
            rawHour > 12 -> rawHour - 12
            else -> rawHour
        }
    }
    val isPm = rawHour >= 12

    val remainingText = remember(rawHour, rawMinute, selectedDays.toList()) {
        AlarmScheduler.getTimeRemainingDescription(rawHour, rawMinute, selectedDays.toList())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (alarm.id == 0L) "New Alarm" else "Edit Alarm",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Alarm $remainingText",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalimBlue
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Time Adjuster Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hours Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                rawHour = if (rawHour == 23) 0 else rawHour + 1
                            },
                            modifier = Modifier.testTag("alarm_hour_increment")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Hour")
                        }
                        Text(
                            text = String.format(Locale.US, "%02d", displayHour),
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 52.sp),
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = {
                                rawHour = if (rawHour == 0) 23 else rawHour - 1
                            },
                            modifier = Modifier.testTag("alarm_hour_decrement")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Subtract Hour")
                        }
                    }

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    // Minutes Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                rawMinute = if (rawMinute == 59) 0 else rawMinute + 1
                            },
                            modifier = Modifier.testTag("alarm_minute_increment")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Minute")
                        }
                        Text(
                            text = String.format(Locale.US, "%02d", rawMinute),
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 52.sp),
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = {
                                rawMinute = if (rawMinute == 0) 59 else rawMinute - 1
                            },
                            modifier = Modifier.testTag("alarm_minute_decrement")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Subtract Minute")
                        }
                    }

                    // AM/PM Toggle if in 12h mode
                    if (!is24Hour) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val amSelected = !isPm
                            val pmSelected = isPm

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (amSelected) SalimBlue else MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        if (isPm) rawHour -= 12
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "AM",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (amSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (pmSelected) SalimBlue else MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        if (!isPm) rawHour += 12
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "PM",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (pmSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Label TextField
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_label_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Repeat Days Section
            Text(
                text = "Repeat",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (dayIndex in 1..7) {
                    val isSelected = selectedDays.contains(dayIndex)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) SalimBlue else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SalimBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable {
                                if (isSelected) selectedDays.remove(dayIndex)
                                else selectedDays.add(dayIndex)
                            }
                            .testTag("alarm_day_chip_$dayIndex"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayLabels[dayIndex - 1],
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            // Vibration Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vibrate",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tactile pattern on ring",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = vibrate,
                    onCheckedChange = { vibrate = it },
                    modifier = Modifier.testTag("alarm_vibrate_switch"),
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SalimBlue)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Snooze Duration Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Snooze Duration",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { mins ->
                        val isSelected = snoozeMinutes == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { snoozeMinutes = mins },
                            label = { Text("${mins}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SalimBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Actions (Save, Cancel, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (alarm.id != 0L && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(alarm)
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                            .testTag("alarm_delete_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Alarm",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("alarm_cancel_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val daysString = selectedDays.sorted().joinToString(",")
                        val updated = alarm.copy(
                            hour = rawHour,
                            minute = rawMinute,
                            label = label.ifBlank { "Alarm" },
                            daysOfWeek = daysString,
                            vibrate = vibrate,
                            snoozeMinutes = snoozeMinutes,
                            ringtoneName = ringtoneName,
                            isEnabled = true
                        )
                        onSave(updated)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("alarm_save_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SalimBlue)
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
