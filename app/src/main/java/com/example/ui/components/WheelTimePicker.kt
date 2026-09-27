package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelTimePicker(
    initialHours: Int,
    initialMinutes: Int,
    initialSeconds: Int,
    onTimeSelected: (hours: Int, minutes: Int, seconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hours Wheel
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HOURS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    NumberWheelColumn(
                        range = 0..23,
                        initialValue = initialHours.coerceIn(0, 23),
                        onValueChange = { h ->
                            onTimeSelected(h, initialMinutes, initialSeconds)
                        },
                        tag = "wheel_hours"
                    )
                }

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )

                // Minutes Wheel
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MINS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    NumberWheelColumn(
                        range = 0..59,
                        initialValue = initialMinutes.coerceIn(0, 59),
                        onValueChange = { m ->
                            onTimeSelected(initialHours, m, initialSeconds)
                        },
                        tag = "wheel_minutes"
                    )
                }

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )

                // Seconds Wheel
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SECS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    NumberWheelColumn(
                        range = 0..59,
                        initialValue = initialSeconds.coerceIn(0, 59),
                        onValueChange = { s ->
                            onTimeSelected(initialHours, initialMinutes, s)
                        },
                        tag = "wheel_seconds"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberWheelColumn(
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    tag: String
) {
    val items = remember(range) { range.toList() }
    val itemHeight = 44.dp
    val visibleItemsCount = 3 // 1 item above, 1 selected in middle, 1 item below

    // We add empty padding placeholders or offset
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = items.indexOf(initialValue).coerceAtLeast(0)
    )
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Center index derived from first visible item
    val centerIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex.coerceIn(0, items.size - 1)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                val validIndex = index.coerceIn(0, items.size - 1)
                onValueChange(items[validIndex])
            }
    }

    Box(
        modifier = Modifier
            .width(72.dp)
            .height(itemHeight * visibleItemsCount)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        // Selection highlight bar in the middle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * visibleItemsCount),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top spacer so index 0 can be centered
            item { Spacer(modifier = Modifier.height(itemHeight)) }

            items(items.size) { index ->
                val itemValue = items[index]
                val isSelected = index == centerIndex
                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.4f,
                    label = "alpha"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 0.85f,
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", itemValue),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 22.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .scale(scale)
                            .alpha(alpha)
                    )
                }
            }

            // Bottom spacer so last index can be centered
            item { Spacer(modifier = Modifier.height(itemHeight)) }
        }
    }
}
