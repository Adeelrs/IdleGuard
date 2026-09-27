package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WheelTimePicker
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    timerTotalSeconds: Long,
    timerRemainingSeconds: Long,
    isTimerRunning: Boolean,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSetDuration: (Int, Int, Int) -> Unit,
    onSnooze: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Picker modal bottom sheet state
    var showTimePickerSheet by remember { mutableStateOf(false) }
    var pickerHours by remember { mutableIntStateOf((timerTotalSeconds / 3600).toInt()) }
    var pickerMinutes by remember { mutableIntStateOf(((timerTotalSeconds % 3600) / 60).toInt()) }
    var pickerSeconds by remember { mutableIntStateOf((timerTotalSeconds % 60).toInt()) }

    val progress = if (timerTotalSeconds > 0) {
        (timerRemainingSeconds.toFloat() / timerTotalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Minimal Header Tag
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTimerRunning) "Timer Running • Lock Screen Active" else "Ready to Focus",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Circular Timer View (Centerpiece)
        Box(
            modifier = Modifier
                .size(280.dp)
                .clickable {
                    // Clicking the timer directly opens the scrollable duration picker!
                    pickerHours = (timerTotalSeconds / 3600).toInt()
                    pickerMinutes = ((timerTotalSeconds % 3600) / 60).toInt()
                    pickerSeconds = (timerTotalSeconds % 60).toInt()
                    showTimePickerSheet = true
                }
                .testTag("home_circular_timer"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                // Background Track
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
                // Progress Arc
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val hours = timerRemainingSeconds / 3600
                val minutes = (timerRemainingSeconds % 3600) / 60
                val seconds = timerRemainingSeconds % 60

                val timeFormatted = if (hours > 0) {
                    String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format(Locale.US, "%02d:%02d", minutes, seconds)
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (hours > 0) 36.sp else 46.sp,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Edit Time",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tap to change duration",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Preset Chips (15m default, 20m, 30m, 45m)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val presets = listOf(15, 20, 30, 45)
            presets.forEach { mins ->
                val isSelected = timerTotalSeconds == (mins * 60L)
                FilterChip(
                    selected = isSelected,
                    onClick = { onSetDuration(0, mins, 0) },
                    label = { Text("${mins}m") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .testTag("preset_${mins}m")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Controls: Start / Pause, Reset, Snooze
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = onResetTimer,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("home_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Main Play / Pause Button
            Button(
                onClick = onToggleTimer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp)
                    .testTag("home_toggle_timer_button")
            ) {
                Icon(
                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isTimerRunning) "Pause Timer" else "Start Timer",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Quick Snooze +5m Button
            IconButton(
                onClick = { onSnooze(5) },
                modifier = Modifier
                    .size(52.dp)
                    .testTag("home_snooze_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreTime,
                    contentDescription = "Snooze +5 mins",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Change Timer (Scroll Picker)" button
        OutlinedButton(
            onClick = {
                pickerHours = (timerTotalSeconds / 3600).toInt()
                pickerMinutes = ((timerTotalSeconds % 3600) / 60).toInt()
                pickerSeconds = (timerTotalSeconds % 60).toInt()
                showTimePickerSheet = true
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("open_scrollable_picker_button")
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Custom Scroll Picker (H:M:S)")
        }
    }

    // Modal Bottom Sheet: Scrollable Hours, Minutes, Seconds Picker
    if (showTimePickerSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showTimePickerSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Timer Duration",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Scroll to set hours, minutes, and seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // Scrollable Wheels
                WheelTimePicker(
                    initialHours = pickerHours,
                    initialMinutes = pickerMinutes,
                    initialSeconds = pickerSeconds,
                    onTimeSelected = { h, m, s ->
                        pickerHours = h
                        pickerMinutes = m
                        pickerSeconds = s
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showTimePickerSheet = false },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSetDuration(pickerHours, pickerMinutes, pickerSeconds)
                            showTimePickerSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_custom_timer_button")
                    ) {
                        Text("Set Timer")
                    }
                }
            }
        }
    }
}
