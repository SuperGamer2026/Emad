package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerTimeCalculator
import com.example.ui.theme.AppTheme
import com.example.ui.components.liquidGlassCard
import com.example.ui.viewmodel.PrayerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun FineTuneScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val offsets by viewModel.offsets.collectAsState()
    val activeThemePreset by viewModel.themePreset.collectAsState()
    val activeAppIconPreset by viewModel.appIconPreset.collectAsState()
    val activeLocationName by viewModel.locationName.collectAsState()
    val activeLat by viewModel.latitude.collectAsState()
    val activeLon by viewModel.longitude.collectAsState()
    val activeTz by viewModel.timezone.collectAsState()

    val baseTimes = remember(activeLat, activeLon, activeTz) {
        PrayerTimeCalculator.calculateTimes(
            LocalDate.now(),
            latitude = activeLat,
            longitude = activeLon,
            timezone = activeTz
        )
    }

    val haptic = LocalHapticFeedback.current

    val adjustables = listOf(
        Triple("FAJR", "Fajr", baseTimes.fajr),
        Triple("SUNRISE", "Sunrise", baseTimes.sunrise),
        Triple("DHUHR", "Dhuhr", baseTimes.dhuhr),
        Triple("ASR", "Asr", baseTimes.asr),
        Triple("MAGHRIB", "Maghrib", baseTimes.maghrib),
        Triple("ISHA", "Isha", baseTimes.isha)
    )

    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    var showClockDialogFor by remember { mutableStateOf<Triple<String, String, java.time.LocalDateTime>?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // Calibration Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(RoundedCornerShape(24.dp), AppTheme.colors.primary)
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
                                text = "CALIBRATION & PRESETS",
                                color = AppTheme.colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Fine-Tuning",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Calibration Settings",
                            tint = AppTheme.colors.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tap on any prayer time block below to enter the exact calculation via an elegant clock dialogue. This instantly updates offsets and schedules precise system alarms.",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Section 1: Themes Switcher
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(RoundedCornerShape(16.dp), AppTheme.colors.primary)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "THEMES",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                val themePresets = listOf(
                    "EXPRESSIVE" to "Expressive",
                    "COSMIC" to "Cosmic Noir",
                    "TEAL" to "Midnight Teal",
                    "ROSE" to "Sunset Rose",
                    "GOLDEN" to "Golden Oasis",
                    "EMERALD" to "Ice Emerald",
                    "OCEAN" to "Pacific Sea",
                    "AMYTHIST" to "Velvet Amethyst",
                    "SUNFIRE" to "Solar Flare",
                    "SAPPHIRE" to "Neon Sapphire",
                    "LAVENDER" to "Lavender Field",
                    "MINT" to "Polar Glacier",
                    "FOREST" to "Pine Evergreen",
                    "BLOOD_MOON" to "Crimson Moon",
                    "MONOCHROME" to "Pure Slate",
                    "SAKURA" to "Tokyo Sakura",
                    "NEBULA" to "Nebula Spark",
                    "DESERT" to "Sahara Nomad",
                    "AURORA" to "Boreal Lights"
                )

                val scrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themePresets.forEach { (key, label) ->
                        val isSelected = activeThemePreset == key
                        val strokeColor = if (isSelected) AppTheme.colors.primary else Color.White.copy(alpha = 0.05f)
                        val colorAccent = when (key) {
                            "EXPRESSIVE" -> Color(0xFF6366F1) to Color(0xFFEC4899)
                            "COSMIC" -> Color(0xFFFF3344) to Color(0xFFFBBF24)
                            "TEAL" -> Color(0xFF06B6D4) to Color(0xFFF43F5E)
                            "ROSE" -> Color(0xFFEC4899) to Color(0xFF8B5CF6)
                            "GOLDEN" -> Color(0xFFD4AF37) to Color(0xFFF59E0B)
                            "EMERALD" -> Color(0xFF10B981) to Color(0xFF34D399)
                            "OCEAN" -> Color(0xFF3B82F6) to Color(0xFF60A5FA)
                            "AMYTHIST" -> Color(0xFF8B5CF6) to Color(0xFFD8B4FE)
                            "SUNFIRE" -> Color(0xFFEF4444) to Color(0xFFF97316)
                            "SAPPHIRE" -> Color(0xFF2563EB) to Color(0xFFA5F3FC)
                            "LAVENDER" -> Color(0xFFA78BFA) to Color(0xFFF472B6)
                            "MINT" -> Color(0xFF06B6D4) to Color(0xFF99F6E4)
                            "FOREST" -> Color(0xFF4ADE80) to Color(0xFFFACC15)
                            "BLOOD_MOON" -> Color(0xFFDC2626) to Color(0xFF9CA3AF)
                            "MONOCHROME" -> Color(0xFFE2E8F0) to Color(0xFF94A3B8)
                            "SAKURA" -> Color(0xFFFBCFE8) to Color(0xFFFDA4AF)
                            "NEBULA" -> Color(0xFFC084FC) to Color(0xFF818CF8)
                            "DESERT" -> Color(0xFFF59E0B) to Color(0xFFD97706)
                            "AURORA" -> Color(0xFF2DD4BF) to Color(0xFFC084FC)
                            else -> Color(0xFF6366F1) to Color(0xFFEC4899)
                        }

                        Box(
                            modifier = Modifier
                                .width(96.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppTheme.colors.primary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.02f))
                                .border(if (isSelected) 2.dp else 1.dp, strokeColor, RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setThemePreset(key)
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(colorAccent.first)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(colorAccent.second)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else AppTheme.colors.grayMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // App Icon Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(RoundedCornerShape(16.dp), AppTheme.colors.secondary)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = AppTheme.colors.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APP ICON",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                val themePresets = listOf(
                    "EXPRESSIVE" to "Expressive",
                    "COSMIC" to "Cosmic Noir",
                    "TEAL" to "Midnight Teal",
                    "ROSE" to "Sunset Rose",
                    "GOLDEN" to "Golden Oasis",
                    "EMERALD" to "Ice Emerald",
                    "OCEAN" to "Pacific Sea",
                    "AMYTHIST" to "Velvet Amethyst",
                    "SUNFIRE" to "Solar Flare",
                    "SAPPHIRE" to "Neon Sapphire",
                    "LAVENDER" to "Lavender Field",
                    "MINT" to "Polar Glacier",
                    "FOREST" to "Pine Evergreen",
                    "BLOOD_MOON" to "Crimson Moon",
                    "MONOCHROME" to "Pure Slate",
                    "SAKURA" to "Tokyo Sakura",
                    "NEBULA" to "Nebula Spark",
                    "DESERT" to "Sahara Nomad",
                    "AURORA" to "Boreal Lights"
                )

                val scrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themePresets.forEach { (key, label) ->
                        val isSelected = activeAppIconPreset == key
                        val strokeColor = if (isSelected) AppTheme.colors.secondary else Color.White.copy(alpha = 0.05f)
                        val colorAccent = when (key) {
                            "COSMIC" -> Color(0xFFFF3344)
                            "TEAL" -> Color(0xFF06B6D4)
                            "ROSE" -> Color(0xFFEC4899)
                            "GOLDEN" -> Color(0xFFD4AF37)
                            "EMERALD" -> Color(0xFF10B981)
                            "OCEAN" -> Color(0xFF3B82F6)
                            "AMYTHIST" -> Color(0xFF8B5CF6)
                            "SUNFIRE" -> Color(0xFFEF4444)
                            "SAPPHIRE" -> Color(0xFF2563EB)
                            "LAVENDER" -> Color(0xFFA78BFA)
                            "MINT" -> Color(0xFF06B6D4)
                            "FOREST" -> Color(0xFF4ADE80)
                            "BLOOD_MOON" -> Color(0xFFDC2626)
                            "MONOCHROME" -> Color(0xFFE2E8F0)
                            "SAKURA" -> Color(0xFFFBCFE8)
                            "NEBULA" -> Color(0xFFC084FC)
                            "DESERT" -> Color(0xFFF59E0B)
                            "AURORA" -> Color(0xFF2DD4BF)
                            else -> Color(0xFF6366F1)
                        }

                        Box(
                            modifier = Modifier
                                .width(96.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppTheme.colors.secondary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.02f))
                                .border(if (isSelected) 2.dp else 1.dp, strokeColor, RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setAppIconPreset(key)
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF020104)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    val iconResId = remember(key) {
                                        val iconName = "ic_launcher_foreground_${if (key == "BLOOD_MOON") "blood_moon" else key.lowercase()}"
                                        val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                                        if (resId != 0) resId else com.example.R.drawable.ic_launcher_foreground_expressive
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color(0xFF0A0A0A))
                                    )
                                    
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = iconResId),
                                        contentDescription = "App Icon Preview",
                                        modifier = Modifier.matchParentSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else AppTheme.colors.grayMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Location Selector
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(RoundedCornerShape(16.dp), AppTheme.colors.secondary)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AppTheme.colors.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOCATION REGION CONFIG",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Current: $activeLocationName (${String.format("%.4f", activeLat)}, ${String.format("%.4f", activeLon)})",
                    color = AppTheme.colors.grayMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // List of preset Locations
                val locations = listOf(
                    Triple("Aleppo, Syria", 36.2021 to 37.1343, 3.0),
                    Triple("Mecca, Saudi Arabia", 21.4225 to 39.8262, 3.0),
                    Triple("Cairo, Egypt", 30.0444 to 31.2357, 2.0),
                    Triple("Istanbul, Turkey", 41.0082 to 28.9784, 3.0)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    locations.forEach { (name, coords, tz) ->
                        val isSelected = activeLocationName == name
                        val strokeColor = if (isSelected) AppTheme.colors.secondary else Color.White.copy(alpha = 0.05f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppTheme.colors.secondary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.02f))
                                .border(1.dp, strokeColor, RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setLocation(name, coords.first, coords.second, tz)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.split(",")[0],
                                color = if (isSelected) Color.White else AppTheme.colors.grayMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Subtitle Offset controllers
        item {
            Text(
                text = "${activeLocationName.uppercase().split(",")[0]} TIME OFFSET CONTROLLERS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Configuration Rows
        items(adjustables) { (key, label, baseDateTime) ->
            val offsetVal = offsets[key] ?: 0
            val calibratedTime = baseDateTime.plusMinutes(offsetVal.toLong())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(RoundedCornerShape(18.dp), AppTheme.colors.primary)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showClockDialogFor = Triple(key, label, baseDateTime)
                    }
                    .testTag("offset_card_${key.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Base: ${baseDateTime.format(formatter)}",
                                color = AppTheme.colors.grayMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(AppTheme.colors.grayMuted, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Calibrated: ${calibratedTime.format(formatter)}",
                                color = if (offsetVal != 0) AppTheme.colors.secondary else AppTheme.colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Replaced + and - buttons with interactive time preview badge & Edit icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (offsetVal != 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppTheme.colors.secondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (offsetVal > 0) "+$offsetVal m" else "$offsetVal m",
                                    color = AppTheme.colors.secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = calibratedTime.format(formatter),
                            color = if (offsetVal != 0) AppTheme.colors.secondary else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("time_text_${key.lowercase()}")
                        )

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Time Popup",
                            tint = AppTheme.colors.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showClockDialogFor != null) {
        val target = showClockDialogFor!!
        ClockPopupDialog(
            label = target.second,
            baseDateTime = target.third,
            currentOffset = offsets[target.first] ?: 0,
            onDismiss = { showClockDialogFor = null },
            onSave = { newOffset ->
                viewModel.updateOffset(target.first, newOffset)
                showClockDialogFor = null
            }
        )
    }
}

@Composable
fun ClockPopupDialog(
    label: String,
    baseDateTime: java.time.LocalDateTime,
    currentOffset: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val baseHour = baseDateTime.hour
    val baseMinute = baseDateTime.minute
    
    val currentCalibrated = baseDateTime.plusMinutes(currentOffset.toLong())
    
    var hourText by remember { mutableStateOf(String.format("%02d", currentCalibrated.hour)) }
    var minuteText by remember { mutableStateOf(String.format("%02d", currentCalibrated.minute)) }
    
    val parsedHour = hourText.toIntOrNull()
    val parsedMinute = minuteText.toIntOrNull()
    
    val isValid = parsedHour != null && parsedHour in 0..23 && parsedMinute != null && parsedMinute in 0..59
    
    val calculatedOffset = if (isValid) {
        val baseMin = baseHour * 60 + baseMinute
        val targetMin = parsedHour!! * 60 + parsedMinute!!
        targetMin - baseMin
    } else {
        0
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surface,
        title = {
            Column {
                Text(
                    text = "CALIBRATE ${label.uppercase()}",
                    color = AppTheme.colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Adjust Exact Time",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Base standard calculation: ${String.format("%02d:%02d", baseHour, baseMinute)}",
                    color = AppTheme.colors.grayMuted,
                    fontSize = 12.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hour inputs
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val current = hourText.toIntOrNull() ?: 0
                                val next = (current + 1) % 24
                                hourText = String.format("%02d", next)
                            },
                            modifier = Modifier.size(36.dp).testTag("hour_plus")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increment Hour",
                                tint = AppTheme.colors.primary
                            )
                        }
                        
                        OutlinedTextField(
                            value = hourText,
                            onValueChange = { input ->
                                if (input.length <= 2 && input.all { it.isDigit() }) {
                                    hourText = input
                                }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AppTheme.colors.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                unfocusedContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .width(80.dp)
                                .testTag("dialog_hour_input")
                        )
                        
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val current = hourText.toIntOrNull() ?: 0
                                val prev = (current - 1 + 24) % 24
                                hourText = String.format("%02d", prev)
                            },
                            modifier = Modifier.size(36.dp).testTag("hour_minus")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrement Hour",
                                tint = AppTheme.colors.primary
                            )
                        }
                        
                        Text("HOUR", color = AppTheme.colors.grayMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Text(
                        text = ":",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                    )
                    
                    // Minute inputs
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val current = minuteText.toIntOrNull() ?: 0
                                val next = (current + 1) % 60
                                minuteText = String.format("%02d", next)
                            },
                            modifier = Modifier.size(36.dp).testTag("minute_plus")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increment Minute",
                                tint = AppTheme.colors.primary
                            )
                        }
                        
                        OutlinedTextField(
                            value = minuteText,
                            onValueChange = { input ->
                                if (input.length <= 2 && input.all { it.isDigit() }) {
                                    minuteText = input
                                }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AppTheme.colors.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                unfocusedContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .width(80.dp)
                                .testTag("dialog_minute_input")
                        )
                        
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val current = minuteText.toIntOrNull() ?: 0
                                val prev = (current - 1 + 60) % 60
                                minuteText = String.format("%02d", prev)
                            },
                            modifier = Modifier.size(36.dp).testTag("minute_minus")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrement Minute",
                                tint = AppTheme.colors.primary
                            )
                        }
                        
                        Text("MINUTE", color = AppTheme.colors.grayMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (isValid) {
                    val shiftLabel = if (calculatedOffset >= 0) "+$calculatedOffset min" else "$calculatedOffset min"
                    val shiftColor = if (calculatedOffset == 0) AppTheme.colors.primary else AppTheme.colors.secondary
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(shiftColor.copy(alpha = 0.05f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Resulting shift: $shiftLabel",
                            color = shiftColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Text(
                        text = "Invalid Time! Must be 00-23 and 00-59",
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onSave(calculatedOffset)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary,
                    disabledContainerColor = AppTheme.colors.surface,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_save_btn")
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_btn")
            ) {
                Text("CANCEL", color = AppTheme.colors.grayMuted)
            }
        }
    )
}
