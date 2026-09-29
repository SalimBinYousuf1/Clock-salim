package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.UserSettings
import com.example.ui.theme.SalimBlue

@Composable
fun SettingsDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    onToggle24Hour: (Boolean) -> Unit,
    onToggleSeconds: (Boolean) -> Unit,
    onSetSnoozeMinutes: (Int) -> Unit,
    onToggleVibrate: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onSetThemeMode: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("settings_close_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close Settings")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Time Display
                Text(
                    text = "TIME DISPLAY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SalimBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "24-Hour Time",
                    subtitle = "Display time in 24-hour format (e.g. 14:00)",
                    checked = settings.is24HourFormat,
                    onCheckedChange = onToggle24Hour,
                    testTag = "setting_24_hour_toggle"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "Show Seconds",
                    subtitle = "Precision seconds ticker on main clock",
                    checked = settings.showSeconds,
                    onCheckedChange = onToggleSeconds,
                    testTag = "setting_show_seconds_toggle"
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Alarms & Timers
                Text(
                    text = "ALARMS & SNOOZE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SalimBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Text(
                        text = "Default Snooze Duration",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 20).forEach { mins ->
                            val isSelected = settings.defaultSnoozeMinutes == mins
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetSnoozeMinutes(mins) },
                                label = { Text("${mins}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SalimBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "Alarm Vibration",
                    subtitle = "Vibrate device during alarms and timer alerts",
                    checked = settings.vibrateEnabled,
                    onCheckedChange = onToggleVibrate,
                    testTag = "setting_vibrate_toggle"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "Haptic Feedback",
                    subtitle = "Subtle tactile response when pressing controls",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    testTag = "setting_haptics_toggle"
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Appearance
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SalimBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SYSTEM" to "System", "DARK" to "Dark", "LIGHT" to "Light").forEach { (code, label) ->
                        val isSelected = settings.themeMode == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetThemeMode(code) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SalimBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Salim Native Precision Clock • v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SalimBlue)
        )
    }
}
