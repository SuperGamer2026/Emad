package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppTheme
import com.example.ui.components.liquidGlassCard
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.viewmodel.DailyPillarData
import com.example.data.local.model.PrayerRecord
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: PrayerViewModel,
    onTierClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val countdownState by viewModel.countdownState.collectAsState()
    val todayRecords by viewModel.todayRecords.collectAsState()
    val streak by viewModel.streakCount.collectAsState()
    val levelTitle by viewModel.consistencyLevelTitle.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val weeklyPillarStats by viewModel.weeklyPillarStats.collectAsState()
    val haptic = LocalHapticFeedback.current

    val fajrNotif by viewModel.fahrNotifEnabled.collectAsState()
    val dhuhrNotif by viewModel.dhuhrNotifEnabled.collectAsState()
    val asrNotif by viewModel.asrNotifEnabled.collectAsState()
    val maghribNotif by viewModel.maghribNotifEnabled.collectAsState()
    val ishaNotif by viewModel.ishaNotifEnabled.collectAsState()

    var showFullScreenFire by remember { mutableStateOf(false) }
    var previousStreak by remember { mutableStateOf(streak) }
    var isReadyToFire by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        isReadyToFire = true
    }

    LaunchedEffect(streak) {
        if (isReadyToFire && previousStreak != -1 && streak > previousStreak) {
            showFullScreenFire = true
        }
        previousStreak = streak
    }

    if (showFullScreenFire) {
        FullScreenFireOverlay(
            isVisible = showFullScreenFire,
            onFinished = { showFullScreenFire = false }
        )
    }

    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    val fardCompleted = remember(todayRecords) {
        val fardKeys = listOf("FAJR_FARD", "DHUHR_FARD", "ASR_FARD", "MAGHRIB_FARD", "ISHA_FARD")
        todayRecords.count { fardKeys.contains(it.prayerKey) && (it.status == "PRAYED_ON_TIME" || it.status == "PRAYED_LATE") }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURRENT LOCATION",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = locationName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Streak Banner
                Row(
                    modifier = Modifier
                        .liquidGlassCard(RoundedCornerShape(20.dp), AppTheme.colors.primary)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("streak_badge"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (streak > 0) {
                        ThemedFireAnimation(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Fire",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$streak DAY STREAK",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Countdown Block
        item {
            val primaryColor = AppTheme.colors.primary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .liquidGlassCard(RoundedCornerShape(28.dp), AppTheme.colors.primary)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background radial glow effect
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .drawBehind {
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.03f),
                                radius = size.minDimension / 2
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.04f),
                                radius = size.minDimension / 2,
                                style = Stroke(
                                    width = 1f,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                        floatArrayOf(10f, 10f),
                                        0f
                                    )
                                )
                            )
                        }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "TIME UNTIL ${countdownState.nextPrayerName.uppercase()}",
                        color = AppTheme.colors.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = countdownState.formattedRemaining,
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )
                        Text(
                            text = " NEXT CALL: ${countdownState.nextPrayerTime} ",
                            color = AppTheme.colors.grayMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )
                    }
                }

                // Sleek progress bar/ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawArc(
                                color = primaryColor.copy(alpha = 0.08f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx())
                            )
                            drawArc(
                                color = primaryColor,
                                startAngle = -90f,
                                sweepAngle = 360f * countdownState.depletingRatio,
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }
                )
            }
        }

        // Streak Active Fire Banner
        if (streak > 0) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(RoundedCornerShape(24.dp), AppTheme.colors.secondary)
                        .padding(16.dp)
                        .testTag("streak_fire_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STREAK IS ALIVE!",
                            color = AppTheme.colors.secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your Spiritual Fire is Ablaze",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Maintain prayers on-time to fuel this flame and sustain your $streak-day streak.",
                            color = AppTheme.colors.grayMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    ThemedFireAnimation(
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        // Mini Metrics & Tiers Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlassCard(RoundedCornerShape(18.dp), AppTheme.colors.primary)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "FARDH COMPLETED",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$fardCompleted/5",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .align(Alignment.CenterVertically)
                        ) {
                            val ratioProgress = (fardCompleted.toFloat() / 5f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratioProgress)
                                    .background(AppTheme.colors.primary)
                            )
                        }
                    }
                }

                // Card 2
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlassCard(RoundedCornerShape(18.dp), AppTheme.colors.secondary)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTierClick()
                        }
                        .padding(14.dp)
                ) {
                    Text(
                        text = "TIER STATUS",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = levelTitle,
                        color = AppTheme.colors.secondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Weekly Prayer Pillars Chart
        item {
            WeeklyPrayerPillars(
                pillarStats = weeklyPillarStats,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            )
        }

        // Ledger Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S LEDGER",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "${locationName.uppercase().split(",")[0]} STANDARDS",
                    color = AppTheme.colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Grouped daily ledger list
        val dailyGroups = getDailyGroups()
        items(dailyGroups) { group ->
            val isExpanded = expandedGroups[group.baseKey] ?: false
            val isNotifEnabled = when (group.title) {
                "Fajr" -> fajrNotif
                "Dhuhr" -> dhuhrNotif
                "Asr" -> asrNotif
                "Maghrib" -> maghribNotif
                "Isha" -> ishaNotif
                else -> true
            }
            PrayerGroupCard(
                group = group,
                todayRecords = todayRecords,
                isExpanded = isExpanded,
                onToggleExpand = {
                    expandedGroups[group.baseKey] = !isExpanded
                },
                onStatusChange = { key, status ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.updatePrayerStatus(key, status)
                },
                isNotifEnabled = isNotifEnabled,
                onToggleNotif = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.togglePrayerNotification(group.title)
                }
            )
        }
    } // Closes LazyColumn
} // Closes Box
} // Closes HomeScreen

// Model & Data Helper for Groups
data class PrayerGroup(
    val title: String,
    val arabicTitle: String,
    val baseKey: String,
    val components: List<PrayerComponent>
)

data class PrayerComponent(
    val key: String,
    val label: String,
    val subtitle: String,
    val isFard: Boolean
)

private fun getDailyGroups(): List<PrayerGroup> {
    return listOf(
        PrayerGroup(
            title = "Fajr",
            arabicTitle = "الْفَجْر",
            baseKey = "FAJR",
            components = listOf(
                PrayerComponent("FAJR_SUNNAH", "Sunnah (Before)", "2 Rakaats • Emphasized Sunnah", false),
                PrayerComponent("FAJR_FARD", "Fajr Fardh", "2 Rakaats • Mandatory", true)
            )
        ),
        PrayerGroup(
            title = "Dhuhr",
            arabicTitle = "الظُّهْر",
            baseKey = "DHUHR",
            components = listOf(
                PrayerComponent("DHUHR_SUNNAH_BEFORE", "Sunnah (Before)", "4 Rakaats • Emphasized Sunnah", false),
                PrayerComponent("DHUHR_FARD", "Dhuhr Fardh", "4 Rakaats • Mandatory", true),
                PrayerComponent("DHUHR_SUNNAH_AFTER", "Sunnah (After)", "2 Rakaats • Emphasized Sunnah", false)
            )
        ),
        PrayerGroup(
            title = "Asr",
            arabicTitle = "الْعَصْر",
            baseKey = "ASR",
            components = listOf(
                PrayerComponent("ASR_FARD", "Asr Fardh", "4 Rakaats • Mandatory", true)
            )
        ),
        PrayerGroup(
            title = "Maghrib",
            arabicTitle = "الْمَغْرِب",
            baseKey = "MAGHRIB",
            components = listOf(
                PrayerComponent("MAGHRIB_FARD", "Maghrib Fardh", "3 Rakaats • Mandatory", true),
                PrayerComponent("MAGHRIB_SUNNAH", "Sunnah (After)", "2 Rakaats • Emphasized Sunnah", false)
            )
        ),
        PrayerGroup(
            title = "Isha",
            arabicTitle = "الْعِشَاء",
            baseKey = "ISHA",
            components = listOf(
                PrayerComponent("ISHA_SUNNAH_BEFORE", "Sunnah (Before)", "4 Rakaats • Optional Sunnah", false),
                PrayerComponent("ISHA_FARD", "Isha Fardh", "4 Rakaats • Mandatory", true),
                PrayerComponent("ISHA_SUNNAH_AFTER", "Sunnah (After)", "2 Rakaats • Emphasized Sunnah", false),
                PrayerComponent("ISHA_WITR", "Witr", "3 Rakaats • Highly Emphasized Wajib", false)
            )
        )
    )
}

@Composable
fun PrayerGroupCard(
    group: PrayerGroup,
    todayRecords: List<PrayerRecord>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onStatusChange: (String, String) -> Unit,
    isNotifEnabled: Boolean,
    onToggleNotif: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(RoundedCornerShape(22.dp), AppTheme.colors.primary)
            .clickable { onToggleExpand() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Group Title
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { onToggleNotif() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("bell_icon_${group.title.lowercase()}")
                    ) {
                        Icon(
                            imageVector = if (isNotifEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Toggle Notification",
                            tint = if (isNotifEnabled) AppTheme.colors.secondary else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = group.title.uppercase(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        // Show quick completion text
                        val fardComp = group.components.filter { it.isFard }.all { comp ->
                            val statusRecord = todayRecords.firstOrNull { it.prayerKey == comp.key }
                            statusRecord != null && (statusRecord.status == "PRAYED_ON_TIME" || statusRecord.status == "PRAYED_LATE")
                        }
                        val summaryText = if (fardComp) "Fardh Completed" else "Fardh Unmarked"
                        Text(
                            text = summaryText,
                            color = if (fardComp) AppTheme.colors.success else Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = group.arabicTitle,
                        color = AppTheme.colors.primary.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Arrow",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable sub-components panel
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.05f))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sub-components
                    group.components.forEach { comp ->
                        val statusRecord = todayRecords.firstOrNull { it.prayerKey == comp.key }
                        val currentStatus = statusRecord?.status ?: "MUTED_NOT_PRAYED"

                        PrayerComponentItem(
                            component = comp,
                            currentStatus = currentStatus,
                            onStatusSelected = { newStatus ->
                                onStatusChange(comp.key, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerComponentItem(
    component: PrayerComponent,
    currentStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (currentStatus) {
        "PRAYED_ON_TIME" -> AppTheme.colors.success
        "PRAYED_LATE" -> AppTheme.colors.secondary
        "QADA" -> AppTheme.colors.primary
        else -> AppTheme.colors.grayMuted
    }

    val statusName = when (currentStatus) {
        "PRAYED_ON_TIME" -> "On Time"
        "PRAYED_LATE" -> "Late"
        "QADA" -> "Qada"
        else -> "Unmarked"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Outer glow dots indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .drawBehind {
                            drawCircle(
                                color = statusColor.copy(alpha = 0.3f),
                                radius = size.minDimension * 1.5f
                            )
                            drawCircle(color = statusColor, radius = size.minDimension / 2)
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = component.label,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (component.isFard) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FARDH",
                                color = AppTheme.colors.primary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(AppTheme.colors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = component.subtitle,
                        color = AppTheme.colors.grayMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Status chip button
            Box(
                modifier = Modifier
                    .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusName.uppercase(),
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        // Expandable selector panel
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = "SELECT STATUS FOR THIS PRAYER:",
                    color = AppTheme.colors.grayMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val states = listOf(
                        Triple("PRAYED_ON_TIME", "ON TIME", AppTheme.colors.success),
                        Triple("PRAYED_LATE", "LATE", AppTheme.colors.secondary),
                        Triple("QADA", "QADA", AppTheme.colors.primary),
                        Triple("MUTED_NOT_PRAYED", "RESET", AppTheme.colors.grayMuted)
                    )

                    states.forEach { (stateKey, label, color) ->
                        val isSelected = currentStatus == stateKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) color.copy(alpha = 0.2f) else AppTheme.colors.surface.copy(
                                        alpha = 0.5f
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) color else color.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onStatusSelected(stateKey)
                                    expanded = false
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) color else AppTheme.colors.onBackground.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyPrayerPillars(
    pillarStats: List<DailyPillarData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassCard(RoundedCornerShape(24.dp), AppTheme.colors.primary)
            .padding(16.dp)
            .testTag("weekly_pillars_chart")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HABIT PILLARS",
                    color = AppTheme.colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Pillar Stars",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Fardh & Sunnah (Last 7 Days)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.primary)
                    )
                    Text(
                        text = "F",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.secondary)
                    )
                    Text(
                        text = "S",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Area - Columns/Pillars side-by-side representing each of the 7 days
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            pillarStats.forEach { dayData ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Pillars Container
                    Row(
                        modifier = Modifier
                            .height(90.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Fardh Pillar (Maximum 5)
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                .background(Color.White.copy(alpha = 0.04f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val fardRatio = (dayData.fardCompleted.toFloat() / 5f).coerceIn(0f, 1f)
                            if (fardRatio > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fardRatio)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    AppTheme.colors.primary.copy(alpha = 0.95f),
                                                    AppTheme.colors.primary.copy(alpha = 0.35f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        // Sunnah Pillar (Maximum 6)
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                .background(Color.White.copy(alpha = 0.04f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val sunnahRatio = (dayData.sunnahCompleted.toFloat() / 6f).coerceIn(0f, 1f)
                            if (sunnahRatio > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(sunnahRatio)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    AppTheme.colors.secondary.copy(alpha = 0.95f),
                                                    AppTheme.colors.secondary.copy(alpha = 0.35f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Date Day Label
                    Text(
                        text = dayData.dateLabel.uppercase(),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun ThemedFireAnimation(
    modifier: Modifier = Modifier,
    flameColorPrimary: Color = AppTheme.colors.primary,
    flameColorSecondary: Color = AppTheme.colors.secondary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fire_anim")
    
    // Wave oscillation for flame flicker
    val flicker1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker1"
    )
    
    val flicker2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker2"
    )

    val wanderX1 by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wanderX1"
    )

    // Animated particles rising up
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    Canvas(
        modifier = modifier
            .testTag("themed_fire_animation")
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val bottomY = height * 0.9f
        
        // --- 1. Draw glowing background ---
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(flameColorPrimary.copy(alpha = 0.25f), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(centerX, bottomY - height * 0.3f),
                radius = width * 0.7f
            )
        )

        // --- 2. Outer Flame (Teardrop shape scale-animated) ---
        val outerPath = Path().apply {
            val outerW = width * 0.55f * flicker1
            val outerH = height * 0.75f * flicker2
            moveTo(centerX, bottomY - outerH)
            cubicTo(
                centerX + outerW * 0.5f + wanderX1, bottomY - outerH * 0.6f,
                centerX + outerW * 0.6f, bottomY,
                centerX, bottomY
            )
            cubicTo(
                centerX - outerW * 0.6f, bottomY,
                centerX - outerW * 0.5f + wanderX1, bottomY - outerH * 0.6f,
                centerX, bottomY - outerH
            )
            close()
        }
        drawPath(
            path = outerPath,
            brush = Brush.verticalGradient(
                colors = listOf(flameColorSecondary.copy(alpha = 0.1f), flameColorPrimary.copy(alpha = 0.7f)),
                startY = bottomY - height * 0.8f,
                endY = bottomY
            )
        )

        // --- 3. Middle Flame (Slightly smaller, hotter colors) ---
        val middlePath = Path().apply {
            val middleW = width * 0.38f * flicker2
            val middleH = height * 0.55f * flicker1
            moveTo(centerX, bottomY - middleH)
            cubicTo(
                centerX + middleW * 0.5f - wanderX1 * 0.5f, bottomY - middleH * 0.6f,
                centerX + middleW * 0.6f, bottomY,
                centerX, bottomY
            )
            cubicTo(
                centerX - middleW * 0.6f, bottomY,
                centerX - middleW * 0.5f - wanderX1 * 0.5f, bottomY - middleH * 0.6f,
                centerX, bottomY - middleH
            )
            close()
        }
        drawPath(
            path = middlePath,
            brush = Brush.verticalGradient(
                colors = listOf(flameColorSecondary.copy(alpha = 0.5f), flameColorPrimary),
                startY = bottomY - height * 0.6f,
                endY = bottomY
            )
        )

        // --- 4. Inner Core (Very small, hottest center) ---
        val innerPath = Path().apply {
            val innerW = width * 0.22f * flicker1
            val innerH = height * 0.35f * flicker1
            moveTo(centerX, bottomY - innerH)
            cubicTo(
                centerX + innerW * 0.5f, bottomY - innerH * 0.4f,
                centerX + innerW * 0.5f, bottomY,
                centerX, bottomY
            )
            cubicTo(
                centerX - innerW * 0.5f, bottomY,
                centerX - innerW * 0.5f, bottomY - innerH * 0.4f,
                centerX, bottomY - innerH
            )
            close()
        }
        drawPath(
            path = innerPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, flameColorSecondary),
                startY = bottomY - height * 0.4f,
                endY = bottomY
            )
        )

        // --- 5. Rising Sparks ---
        val sparks = listOf(
            Ember(-0.25f, 0.7f, 3.dp.toPx(), 1.0f),
            Ember(0.2f, 0.5f, 2.dp.toPx(), 1.2f),
            Ember(-0.05f, 0.9f, 1.dp.toPx(), 0.8f)
        )

        sparks.forEach { spark ->
            val prog = (particleProgress * spark.speed) % 1f
            val sparkY = bottomY - (spark.startYMult * height * prog)
            val sparkX = centerX + (spark.offsetMult * width) + (kotlin.math.sin(prog * 6.2831853f) * 6f)
            val alpha = (1f - prog).coerceIn(0f, 1f)
            
            if (sparkY > 0f && alpha > 0f) {
                drawCircle(
                    color = flameColorSecondary.copy(alpha = alpha),
                    radius = spark.radiusPx,
                    center = androidx.compose.ui.geometry.Offset(sparkX, sparkY)
                )
            }
        }
    }
}

private data class Ember(
    val offsetMult: Float,
    val startYMult: Float,
    val radiusPx: Float,
    val speed: Float
)
