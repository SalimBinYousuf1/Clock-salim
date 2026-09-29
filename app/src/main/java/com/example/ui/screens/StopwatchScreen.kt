package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimRed
import com.example.viewmodel.LapRecord
import com.example.viewmodel.StopwatchState
import com.example.viewmodel.StopwatchViewModel

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel
) {
    val stopwatchState by viewModel.stopwatchState.collectAsState()
    val totalElapsed by viewModel.totalElapsedMillis.collectAsState()
    val currentLapElapsed by viewModel.currentLapElapsedMillis.collectAsState()
    val laps by viewModel.laps.collectAsState()

    val formattedTotal = StopwatchViewModel.formatStopwatchTime(totalElapsed)
    val formattedLap = StopwatchViewModel.formatStopwatchTime(currentLapElapsed)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Stopwatch",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Focal Point: Tabular Centiseconds Elapsed Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.testTag("stopwatch_display")
        ) {
            Text(
                text = formattedTotal,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 62.sp),
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subordinate current lap split
            if (stopwatchState != StopwatchState.IDLE) {
                Text(
                    text = "Current lap: $formattedLap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "00:00.00",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Fixed Ergonomic Controls Row (Start / Pause / Resume on Right, Lap / Reset on Left)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action Button: Reset (when paused) or Lap (when running)
            when (stopwatchState) {
                StopwatchState.IDLE -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("stopwatch_lap_button_disabled")
                    ) {
                        Text("Lap", style = MaterialTheme.typography.titleSmall)
                    }
                }
                StopwatchState.RUNNING -> {
                    OutlinedButton(
                        onClick = { viewModel.recordLap() },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("stopwatch_lap_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Lap", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
                StopwatchState.PAUSED -> {
                    OutlinedButton(
                        onClick = { viewModel.reset() },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("stopwatch_reset_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Reset", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Right Action Button: Start / Pause / Resume
            when (stopwatchState) {
                StopwatchState.IDLE -> {
                    Button(
                        onClick = { viewModel.start() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimGreen),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("stopwatch_start_button")
                    ) {
                        Text("Start", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                StopwatchState.RUNNING -> {
                    Button(
                        onClick = { viewModel.pause() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimRed),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("stopwatch_pause_button")
                    ) {
                        Text("Stop", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                StopwatchState.PAUSED -> {
                    Button(
                        onClick = { viewModel.resume() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimGreen),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("stopwatch_resume_button")
                    ) {
                        Text("Resume", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

        // Recorded Laps Table
        if (laps.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp)
            ) {
                items(laps, key = { it.lapIndex }) { lap ->
                    LapRowItem(lap = lap)
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun LapRowItem(lap: LapRecord) {
    val lapColor = when {
        lap.isFastest -> SalimGreen
        lap.isSlowest -> SalimRed
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = String.format("Lap %02d", lap.lapIndex),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (lap.isFastest) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SalimGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FASTEST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SalimGreen
                    )
                }
            } else if (lap.isSlowest) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SalimRed.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SLOWEST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SalimRed
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "+${StopwatchViewModel.formatStopwatchTime(lap.lapTimeMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = lapColor
            )

            Text(
                text = StopwatchViewModel.formatStopwatchTime(lap.totalTimeMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
