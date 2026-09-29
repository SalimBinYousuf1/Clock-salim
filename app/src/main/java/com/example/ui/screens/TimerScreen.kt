package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimOrange
import com.example.ui.theme.SalimRed
import com.example.viewmodel.TimerStatus
import com.example.viewmodel.TimerViewModel
import java.util.Locale

@Composable
fun TimerScreen(
    viewModel: TimerViewModel
) {
    val timerStatus by viewModel.timerStatus.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val totalSeconds by viewModel.totalDurationSeconds.collectAsState()
    val timerLabel by viewModel.timerLabel.collectAsState()
    val presets by viewModel.presets.collectAsState()

    val selectedHours by viewModel.selectedHours.collectAsState()
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val selectedSeconds by viewModel.selectedSeconds.collectAsState()

    var showAddPresetDialog by remember { mutableStateOf(false) }

    val progressFraction = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f

    val animatedSweep by animateFloatAsState(
        targetValue = progressFraction * 360f,
        label = "timerProgressSweep"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Timer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Dial / Progress Display
        Box(
            modifier = Modifier
                .size(280.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            val activeColor = when (timerStatus) {
                TimerStatus.COMPLETED -> SalimRed
                TimerStatus.PAUSED -> SalimOrange
                else -> SalimBlue
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

                // Background track circle
                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )

                // Foreground active countdown arc
                if (timerStatus != TimerStatus.IDLE) {
                    drawArc(
                        color = activeColor,
                        startAngle = -90f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            centerOffset.x - radius,
                            centerOffset.y - radius
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Central time readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (timerStatus == TimerStatus.COMPLETED) {
                    Text(
                        text = "Time's up",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SalimRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timerLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (timerStatus != TimerStatus.IDLE) {
                    Text(
                        text = TimerViewModel.formatRemainingTime(remainingSeconds),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timerLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Interactive Time Picker when IDLE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Hours
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { viewModel.setHours(selectedHours + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Hour")
                            }
                            Text(
                                text = String.format(Locale.US, "%02d", selectedHours),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            IconButton(onClick = { viewModel.setHours(selectedHours - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Subtract Hour")
                            }
                            Text("hours", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Minutes
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { viewModel.setMinutes(selectedMinutes + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Minute")
                            }
                            Text(
                                text = String.format(Locale.US, "%02d", selectedMinutes),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            IconButton(onClick = { viewModel.setMinutes(selectedMinutes - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Subtract Minute")
                            }
                            Text("min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Seconds
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { viewModel.setSeconds(selectedSeconds + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Second")
                            }
                            Text(
                                text = String.format(Locale.US, "%02d", selectedSeconds),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            IconButton(onClick = { viewModel.setSeconds(selectedSeconds - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Subtract Second")
                            }
                            Text("sec", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fixed Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel / Reset
            OutlinedButton(
                onClick = { viewModel.cancelOrReset() },
                enabled = timerStatus != TimerStatus.IDLE,
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp)
                    .testTag("timer_cancel_button")
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Start / Pause / Resume
            when (timerStatus) {
                TimerStatus.IDLE -> {
                    Button(
                        onClick = { viewModel.startTimer() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimGreen),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("timer_start_button")
                    ) {
                        Text("Start", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                TimerStatus.RUNNING -> {
                    Button(
                        onClick = { viewModel.pauseTimer() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimOrange),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("timer_pause_button")
                    ) {
                        Text("Pause", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                TimerStatus.PAUSED -> {
                    Button(
                        onClick = { viewModel.resumeTimer() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimGreen),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("timer_resume_button")
                    ) {
                        Text("Resume", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                TimerStatus.COMPLETED -> {
                    Button(
                        onClick = { viewModel.cancelOrReset() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("timer_done_button")
                    ) {
                        Text("Done", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Presets Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = { showAddPresetDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Custom Preset", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(presets, key = { it.id }) { preset ->
                val mins = preset.durationSeconds / 60
                val secs = preset.durationSeconds % 60
                val durationLabel = if (mins > 0 && secs > 0) "${mins}m ${secs}s" else if (mins > 0) "${mins} min" else "${secs} sec"

                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.startPreset(preset) }
                        .testTag("preset_${preset.name}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = durationLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = SalimBlue
                            )
                        }

                        if (presets.size > 2) {
                            IconButton(
                                onClick = { viewModel.deletePreset(preset) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete preset",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showAddPresetDialog) {
        AddPresetDialog(
            onDismiss = { showAddPresetDialog = false },
            onAdd = { name, durSec ->
                viewModel.addPreset(name, durSec)
                showAddPresetDialog = false
            }
        )
    }
}

@Composable
fun AddPresetDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, durationSec: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var minutes by remember { mutableIntStateOf(5) }
    var seconds by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Timer Preset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name (e.g. Meditation)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minutes.toString(),
                        onValueChange = { minutes = it.toIntOrNull()?.coerceIn(0, 999) ?: 0 },
                        label = { Text("Minutes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = seconds.toString(),
                        onValueChange = { seconds = it.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                        label = { Text("Seconds") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalSec = (minutes * 60) + seconds
                    if (name.isNotBlank() && totalSec > 0) {
                        onAdd(name.trim(), totalSec)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SalimBlue)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
