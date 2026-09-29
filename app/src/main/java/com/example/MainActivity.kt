package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.receiver.AlarmReceiver
import com.example.service.AlarmRingtoneService
import com.example.ui.components.SalimRingingAlarmDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.AlarmScreen
import com.example.ui.screens.ClockScreen
import com.example.ui.screens.StopwatchScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.WorldClockScreen
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimTheme
import com.example.viewmodel.AlarmViewModel
import com.example.viewmodel.ClockViewModel
import com.example.viewmodel.StopwatchViewModel
import com.example.viewmodel.TimerViewModel
import com.example.viewmodel.WorldClockViewModel

enum class SalimTab(val title: String, val icon: ImageVector, val tag: String) {
    CLOCK("Clock", Icons.Default.Schedule, "nav_tab_clock"),
    ALARM("Alarms", Icons.Default.Alarm, "nav_tab_alarms"),
    WORLD_CLOCK("World", Icons.Default.Public, "nav_tab_world_clock"),
    STOPWATCH("Stopwatch", Icons.Default.Timer, "nav_tab_stopwatch"),
    TIMER("Timer", Icons.Default.HourglassEmpty, "nav_tab_timer")
}

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = application as SalimApplication
            val settingsRepo = app.settingsRepository
            val userSettings by settingsRepo.settingsFlow.collectAsState()

            val darkTheme = when (userSettings.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            SalimTheme(darkTheme = darkTheme) {
                // Request Notification Permission on Android 13+
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* Result handled gracefully */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // ViewModels
                val clockViewModel: ClockViewModel = viewModel()
                val alarmViewModel: AlarmViewModel = viewModel()
                val worldClockViewModel: WorldClockViewModel = viewModel()
                val stopwatchViewModel: StopwatchViewModel = viewModel()
                val timerViewModel: TimerViewModel = viewModel()

                var currentTab by remember { mutableStateOf(SalimTab.CLOCK) }
                var showSettingsDialog by remember { mutableStateOf(false) }

                // Check intent extras for direct navigation from notification
                LaunchedEffect(intent) {
                    val navigateTo = intent.getStringExtra("EXTRA_NAVIGATE_TO")
                    if (navigateTo == "timer") {
                        currentTab = SalimTab.TIMER
                    } else if (navigateTo == "alarm") {
                        currentTab = SalimTab.ALARM
                    }
                }

                // BackHandler: returns to Clock tab if on another tab
                BackHandler(enabled = currentTab != SalimTab.CLOCK) {
                    currentTab = SalimTab.CLOCK
                }

                // Active ringing alarm overlay
                val activeRingingAlarm by AlarmRingtoneService.currentRingingAlarm.collectAsState()
                val clockState by clockViewModel.clockState.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "SALIM",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { showSettingsDialog = true },
                                    modifier = Modifier.testTag("open_settings_button")
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            tonalElevation = 0.dp
                        ) {
                            SalimTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.testTag(tab.tag),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SalimBlue,
                                        selectedTextColor = SalimBlue,
                                        indicatorColor = SalimBlue.copy(alpha = 0.12f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            SalimTab.CLOCK -> ClockScreen(
                                viewModel = clockViewModel,
                                onNavigateToAlarms = { currentTab = SalimTab.ALARM },
                                onNavigateToWorldClock = { currentTab = SalimTab.WORLD_CLOCK }
                            )
                            SalimTab.ALARM -> AlarmScreen(
                                viewModel = alarmViewModel,
                                is24Hour = userSettings.is24HourFormat
                            )
                            SalimTab.WORLD_CLOCK -> WorldClockScreen(
                                viewModel = worldClockViewModel
                            )
                            SalimTab.STOPWATCH -> StopwatchScreen(
                                viewModel = stopwatchViewModel
                            )
                            SalimTab.TIMER -> TimerScreen(
                                viewModel = timerViewModel
                            )
                        }
                    }
                }

                // Settings Dialog
                if (showSettingsDialog) {
                    SettingsDialog(
                        settings = userSettings,
                        onDismiss = { showSettingsDialog = false },
                        onToggle24Hour = { settingsRepo.set24HourFormat(it) },
                        onToggleSeconds = { settingsRepo.setShowSeconds(it) },
                        onSetSnoozeMinutes = { settingsRepo.setDefaultSnoozeMinutes(it) },
                        onToggleVibrate = { settingsRepo.setVibrateEnabled(it) },
                        onToggleHaptics = { settingsRepo.setHapticsEnabled(it) },
                        onSetThemeMode = { settingsRepo.setThemeMode(it) }
                    )
                }

                // Active Ringing Alarm Overlay
                if (activeRingingAlarm != null) {
                    SalimRingingAlarmDialog(
                        ringingAlarm = activeRingingAlarm!!,
                        currentTime = clockState.timeFormatted,
                        onDismiss = {
                            AlarmRingtoneService.stopRinging(context)
                        },
                        onSnooze = {
                            val alarm = activeRingingAlarm!!
                            AlarmRingtoneService.stopRinging(context)
                            com.example.alarm.AlarmScheduler.scheduleSnooze(
                                context,
                                alarm.alarmId,
                                alarm.label,
                                alarm.snoozeMinutes,
                                userSettings.vibrateEnabled
                            )
                        }
                    )
                }
            }
        }
    }
}
