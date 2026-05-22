package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.components.liquidGlassCard
import com.example.ui.viewmodel.PrayerViewModel

@Composable
fun QadaForgeScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val qadaTallies by viewModel.qadaTallies.collectAsState()
    val customForgeTarget by viewModel.customForgeTarget.collectAsState()
    val maxQadaTarget by viewModel.maxQadaTarget.collectAsState()
    val haptic = LocalHapticFeedback.current

    val prayers = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")

    val totalPending = remember(qadaTallies) {
        prayers.sumOf { qadaTallies[it] ?: 0 }
    }

    // Default target is the highest number of pending missed prayers (updates dynamically). 
    // If the user configures a custom baseline, we use that instead.
    val forgeTarget = customForgeTarget ?: maxQadaTarget
    var showBaselineDialog by remember { mutableStateOf(false) }

    val percentageCleared = remember(totalPending, forgeTarget) {
        if (forgeTarget <= 0) 100f
        else {
            val cleared = (forgeTarget - totalPending).coerceAtLeast(0)
            ((cleared.toFloat() / forgeTarget.toFloat()) * 100f).coerceIn(0f, 100f)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // Forge Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(RoundedCornerShape(24.dp), AppTheme.colors.secondary)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIQUIDATION QUEST",
                                color = AppTheme.colors.secondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "The Forge",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Build Icon",
                            tint = AppTheme.colors.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "TOTAL PENDING QADA",
                                color = AppTheme.colors.grayMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalPending",
                                color = if (totalPending > 0) AppTheme.colors.primary else AppTheme.colors.success,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Light
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "FORGE TARGET: $forgeTarget",
                                color = AppTheme.colors.grayMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showBaselineDialog = true }
                                    .border(1.dp, AppTheme.colors.grayMuted.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%.1f%% CLEARED", percentageCleared),
                                color = AppTheme.colors.secondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(percentageCleared / 100f)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AppTheme.colors.primary, AppTheme.colors.secondary)
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Checklist Ledger subtitle
        item {
            Text(
                text = "INDIVIDUAL FORGE LEDGERS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 5 Fard items
        items(prayers) { prayerKey ->
            val count = qadaTallies[prayerKey] ?: 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(RoundedCornerShape(18.dp), AppTheme.colors.secondary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = prayerKey,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FARD",
                                color = AppTheme.colors.primary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(AppTheme.colors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = if (count > 0) "$count missed prayers remaining" else "All clear! Alhamdulillah",
                            color = if (count > 0) AppTheme.colors.grayMuted else AppTheme.colors.success,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Tally Counter controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Minus button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                .clickable {
                                    if (count > 0) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.updateQadaCount(prayerKey, -1)
                                    }
                                }
                                .testTag("minus_${prayerKey.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrement missed count",
                                tint = if (count > 0) AppTheme.colors.onBackground else AppTheme.colors.grayMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Current numeric count
                        Text(
                            text = String.format("%02d", count),
                            color = if (count > 0) AppTheme.colors.secondary else AppTheme.colors.success,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center
                        )

                        // Plus button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.updateQadaCount(prayerKey, 1)
                                }
                                .testTag("plus_${prayerKey.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increment missed count",
                                tint = AppTheme.colors.onBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Baseline configuration dialog
    if (showBaselineDialog) {
        var inputVal by remember { mutableStateOf(forgeTarget.toString()) }
        AlertDialog(
            onDismissRequest = { showBaselineDialog = false },
            containerColor = AppTheme.colors.surface,
            title = {
                Text(
                    text = "Configure Total Forge Base Target",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your starting missed prayer tally baseline. Your completion metrics will calculate from this starting depth.",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = { inputVal = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AppTheme.colors.secondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        inputVal.toIntOrNull()?.let {
                            viewModel.saveCustomForgeTarget(it.coerceAtLeast(1))
                        }
                        showBaselineDialog = false
                    }
                ) {
                    Text("SAVE", color = AppTheme.colors.secondary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBaselineDialog = false }) {
                    Text("CANCEL", color = AppTheme.colors.grayMuted)
                }
            }
        )
    }
}
