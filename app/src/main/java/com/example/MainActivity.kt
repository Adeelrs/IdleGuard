package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.IdleTrackingService
import com.example.service.NotificationHelper
import com.example.ui.AppTab
import com.example.ui.IdleGuardViewModel
import com.example.ui.components.SnoozeTriggerDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: IdleGuardViewModel by viewModels {
        val app = application as IdleGuardApplication
        IdleGuardViewModel.Factory(
            repository = app.repository,
            preferencesManager = app.preferencesManager,
            trackerManager = app.trackerManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val preferences by viewModel.preferences.collectAsStateWithLifecycle()

            MyApplicationTheme(colorThemeId = preferences.colorTheme) {
                IdleGuardApp(
                    viewModel = viewModel,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                viewModel.onUserInteraction()
                            }
                        )
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onUserInteraction()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        viewModel.onUserInteraction()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationHelper.EXTRA_OPEN_TRIGGER_DIALOG, false) == true) {
            viewModel.testTriggerNow()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdleGuardApp(
    viewModel: IdleGuardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val snapshot by viewModel.trackerSnapshot.collectAsStateWithLifecycle()

    // Notification Permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && preferences.persistentServiceEnabled) {
            IdleTrackingService.start(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (preferences.persistentServiceEnabled) {
            IdleTrackingService.start(context)
        }
    }

    // Snooze Trigger Modal Dialog (opens whenever trigger fires or test is triggered)
    SnoozeTriggerDialog(
        isOpen = snapshot.isTriggerDialogOpen,
        intervalMinutes = preferences.repeatIntervalMinutes,
        continuousActiveSeconds = snapshot.continuousActiveSeconds,
        onSnooze = { minutes -> viewModel.snooze(minutes) },
        onAcknowledge = { viewModel.acknowledgeNudge() },
        onStartFocus = { /* can snooze or start */ viewModel.snooze(15) },
        onDismiss = { viewModel.dismissTriggerDialog() }
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            AppTab.TASKS -> "Weekly Tasks"
                            AppTab.HOME -> "Focus Timer"
                            AppTab.SETTINGS -> "Settings"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.testTriggerNow() },
                        modifier = Modifier.testTag("top_bar_test_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test Nudge Trigger",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // 1. Tasks Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.TASKS,
                    onClick = { viewModel.selectTab(AppTab.TASKS) },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = "Tasks") },
                    label = { Text("Tasks") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_tasks")
                )

                // 2. Home Tab (Default 15-minute timer)
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { viewModel.selectTab(AppTab.HOME) },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // 3. Settings Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.TASKS -> {
                    TasksScreen(
                        tasks = allTasks,
                        selectedDay = selectedDay,
                        onSelectDay = { day -> viewModel.selectDay(day) },
                        onAddTask = { text -> viewModel.addTask(text) },
                        onToggleTask = { id, done -> viewModel.toggleTaskCompleted(id, done) },
                        onEditTask = { id, title -> viewModel.updateTaskTitle(id, title) },
                        onDeleteTask = { id -> viewModel.deleteTask(id) }
                    )
                }
                AppTab.HOME -> {
                    HomeScreen(
                        timerTotalSeconds = snapshot.timerTotalSeconds,
                        timerRemainingSeconds = snapshot.timerRemainingSeconds,
                        isTimerRunning = snapshot.isTimerRunning,
                        onToggleTimer = { viewModel.toggleTimer() },
                        onResetTimer = { viewModel.resetTimer() },
                        onSetDuration = { h, m, s -> viewModel.setTimerDuration(h, m, s) },
                        onSnooze = { mins -> viewModel.snoozeTimer(mins) }
                    )
                }
                AppTab.SETTINGS -> {
                    SettingsScreen(
                        preferences = preferences,
                        onUpdateRepeatInterval = { mins -> viewModel.updateRepeatInterval(mins) },
                        onUpdateColorTheme = { themeId -> viewModel.updateColorTheme(themeId) }
                    )
                }
            }
        }
    }
}
