package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlarmEntity
import com.example.ui.components.AlarmEditorSheet
import com.example.ui.theme.SalimBlue
import com.example.viewmodel.AlarmViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel,
    is24Hour: Boolean
) {
    val alarms by viewModel.alarms.collectAsState()
    val isEditorOpen by viewModel.isEditorOpen.collectAsState()
    val editingAlarm by viewModel.editingAlarm.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewAlarmEditor() },
                containerColor = SalimBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("create_alarm_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Alarms",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${alarms.count { it.isEnabled }} active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.openNewAlarmEditor() },
                    modifier = Modifier.testTag("add_alarm_top_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (alarms.isEmpty()) {
                // Minimal empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "No Alarms",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Set a scheduled alarm to wake up or stay on track",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { viewModel.openNewAlarmEditor() },
                            colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_state_add_alarm_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Set an Alarm")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmRowCard(
                            alarm = alarm,
                            is24Hour = is24Hour,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onClick = { viewModel.openEditAlarm(alarm) },
                            onDelete = { viewModel.deleteAlarm(alarm) },
                            onDuplicate = { viewModel.duplicateAlarm(alarm) },
                            onTestTrigger = { viewModel.triggerAlarmNow(alarm) }
                        )
                    }
                }
            }
        }
    }

    // Modal sheet for creating/editing alarm
    if (isEditorOpen && editingAlarm != null) {
        AlarmEditorSheet(
            sheetState = sheetState,
            alarm = editingAlarm!!,
            is24Hour = is24Hour,
            onDismiss = { viewModel.closeEditor() },
            onSave = { updated -> viewModel.saveAlarm(updated) },
            onDelete = if (editingAlarm!!.id != 0L) {
                { toDelete -> viewModel.deleteAlarm(toDelete) }
            } else null
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmRowCard(
    alarm: AlarmEntity,
    is24Hour: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onTestTrigger: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val displayHour = if (is24Hour) {
        String.format(Locale.US, "%02d", alarm.hour)
    } else {
        val h = when {
            alarm.hour == 0 -> 12
            alarm.hour > 12 -> alarm.hour - 12
            else -> alarm.hour
        }
        String.format(Locale.US, "%02d", h)
    }
    val displayMinute = String.format(Locale.US, "%02d", alarm.minute)
    val amPm = if (is24Hour) "" else if (alarm.hour >= 12) "PM" else "AM"

    val contentAlpha = if (alarm.isEnabled) 1f else 0.45f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("alarm_item_${alarm.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (alarm.isEnabled) 0.5f else 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Time & AM/PM
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$displayHour:$displayMinute",
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 42.sp),
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                    )
                    if (amPm.isNotEmpty()) {
                        Text(
                            text = amPm,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Label and repeat schedule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                    Text(
                        text = alarm.getDaysSummary(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (alarm.isRepeating()) SalimBlue.copy(alpha = contentAlpha) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                }
            }

            // Controls: Options Menu + Toggle Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("alarm_menu_button_${alarm.id}")
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Alarm Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Test Ring Alarm") },
                            leadingIcon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onTestTrigger()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SalimBlue
                    )
                )
            }
        }
    }
}
