package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.AppTheme

data class LocationPreset(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: Double
)

val OnboardingLocationPresets = listOf(
    LocationPreset("Aleppo, Syria", 36.2021, 37.1343, 3.0),
    LocationPreset("Mecca, Saudi Arabia", 21.4225, 39.8262, 3.0),
    LocationPreset("Medina, Saudi Arabia", 24.4672, 39.6111, 3.0),
    LocationPreset("Cairo, Egypt", 30.0444, 31.2357, 2.0),
    LocationPreset("Istanbul, Turkey", 41.0082, 28.9784, 3.0),
    LocationPreset("London, United Kingdom", 51.5074, -0.1278, 1.0),
    LocationPreset("New York, United States", 40.7128, -74.0060, -4.0)
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: (themePreset: String, location: LocationPreset, notificationsEnabled: Boolean) -> Unit,
    onThemePreview: (String) -> Unit = {},
    onThemeConfirmed: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }
    
    // Setup state configurable during onboarding
    var selectedThemePreset by remember { mutableStateOf("EXPRESSIVE") }
    var selectedLocation by remember { mutableStateOf(OnboardingLocationPresets[0]) }
    var notificationsEnabled by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Request notification permission launcher (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            notificationsEnabled = isGranted
        }
    )

    val totalPages = 4

    LaunchedEffect(currentPage) {
        if (currentPage == 3) {
            kotlinx.coroutines.delay(600) // Ensure enter transition finishes completely so focus is gained
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    notificationsEnabled = true
                }
            } else {
                notificationsEnabled = true
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        containerColor = Color.Transparent,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                // Page indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalPages) { index ->
                        val isSelected = index == currentPage
                        val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp)
                        val color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.grayMuted.copy(alpha = 0.3f)
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Actions (Back, Next/Complete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        TextButton(
                            onClick = { currentPage-- },
                            modifier = Modifier.testTag("onboarding_back_btn")
                        ) {
                            Text(
                                text = "BACK",
                                color = AppTheme.colors.grayMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Skip text
                        TextButton(
                            onClick = { 
                                // Call callback directly with current setups (defaults or whatever they changed)
                                onFinished(selectedThemePreset, selectedLocation, notificationsEnabled)
                            },
                            modifier = Modifier.testTag("onboarding_skip_btn")
                        ) {
                            Text(
                                text = "SKIP",
                                color = AppTheme.colors.grayMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (currentPage < totalPages - 1) {
                                if (currentPage == 0) {
                                    onThemeConfirmed(selectedThemePreset)
                                }
                                currentPage++
                            } else {
                                onFinished(selectedThemePreset, selectedLocation, notificationsEnabled)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.primary,
                            contentColor = AppTheme.colors.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("onboarding_next_btn")
                    ) {
                        Text(
                            text = if (currentPage == totalPages - 1) "COMPLETE SETUP" else "NEXT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> OnboardingWelcomePage(
                        selectedPreset = selectedThemePreset,
                        onPresetSelected = {
                            selectedThemePreset = it
                            onThemePreview(it)
                        }
                    )
                    1 -> OnboardingStatesPage()
                    2 -> OnboardingLocationPage(
                        selectedLocation = selectedLocation,
                        onLocationSelected = { selectedLocation = it }
                    )
                    3 -> OnboardingNotificationPage(
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsToggled = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        notificationsEnabled = true
                                    }
                                } else {
                                    notificationsEnabled = true
                                }
                            } else {
                                notificationsEnabled = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FivePillarsLogo(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val heights = listOf(28.dp, 40.dp, 52.dp, 40.dp, 28.dp)
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(h)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                primaryColor,
                                secondaryColor
                            )
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
        }
    }
}

data class OnboardingThemePreset(
    val key: String,
    val name: String,
    val primary: Color,
    val secondary: Color
)

@Composable
fun OnboardingWelcomePage(
    selectedPreset: String,
    onPresetSelected: (String) -> Unit
) {
    val presets = listOf(
        OnboardingThemePreset("EXPRESSIVE", "Expressive", Color(0xFF6366F1), Color(0xFFEC4899)),
        OnboardingThemePreset("COSMIC", "Cosmic Noir", Color(0xFFFF3344), Color(0xFFFBBF24)),
        OnboardingThemePreset("TEAL", "Midnight Teal", Color(0xFF06B6D4), Color(0xFFF43F5E)),
        OnboardingThemePreset("ROSE", "Sunset Rose", Color(0xFFEC4899), Color(0xFF8B5CF6)),
        OnboardingThemePreset("GOLDEN", "Golden Oasis", Color(0xFFD4AF37), Color(0xFFF59E0B)),
        OnboardingThemePreset("EMERALD", "Ice Emerald", Color(0xFF10B981), Color(0xFF34D399)),
        OnboardingThemePreset("OCEAN", "Pacific Sea", Color(0xFF3B82F6), Color(0xFF60A5FA)),
        OnboardingThemePreset("AMYTHIST", "Velvet Amethyst", Color(0xFF8B5CF6), Color(0xFFD8B4FE)),
        OnboardingThemePreset("SUNFIRE", "Solar Flare", Color(0xFFEF4444), Color(0xFFF97316)),
        OnboardingThemePreset("SAPPHIRE", "Neon Sapphire", Color(0xFF2563EB), Color(0xFFA5F3FC)),
        OnboardingThemePreset("LAVENDER", "Lavender Field", Color(0xFFA78BFA), Color(0xFFF472B6)),
        OnboardingThemePreset("MINT", "Polar Glacier", Color(0xFF06B6D4), Color(0xFF99F6E4)),
        OnboardingThemePreset("FOREST", "Pine Evergreen", Color(0xFF4ADE80), Color(0xFFFACC15)),
        OnboardingThemePreset("BLOOD_MOON", "Crimson Moon", Color(0xFFDC2626), Color(0xFF9CA3AF)),
        OnboardingThemePreset("MONOCHROME", "Pure Slate", Color(0xFFE2E8F0), Color(0xFF94A3B8)),
        OnboardingThemePreset("SAKURA", "Tokyo Sakura", Color(0xFFFBCFE8), Color(0xFFFDA4AF)),
        OnboardingThemePreset("NEBULA", "Nebula Spark", Color(0xFFC084FC), Color(0xFF818CF8)),
        OnboardingThemePreset("DESERT", "Sahara Nomad", Color(0xFFF59E0B), Color(0xFFD97706)),
        OnboardingThemePreset("AURORA", "Boreal Lights", Color(0xFF2DD4BF), Color(0xFFC084FC))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppTheme.colors.primary.copy(alpha = 0.15f),
                            AppTheme.colors.secondary.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            FivePillarsLogo(
                primaryColor = AppTheme.colors.primary,
                secondaryColor = AppTheme.colors.secondary,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "WELCOME TO EMAD",
            color = AppTheme.colors.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "الصلاة عماد الدين",
            color = AppTheme.colors.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Track, perfect, and maintain dynamic prayer consistency in your daily Salah. Define custom offsets and restore missed prayers cleanly.",
            color = AppTheme.colors.grayMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "SELECT YOUR AESTHETIC PRESET (19 THEMES)",
            color = AppTheme.colors.onBackground.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontally scrollable high-fidelity cards
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = selectedPreset == preset.key
                val outlineColor = if (isSelected) AppTheme.colors.primary else Color.White.copy(alpha = 0.08f)
                val bgColor = if (isSelected) AppTheme.colors.primary.copy(alpha = 0.15f) else AppTheme.colors.surface

                Card(
                    modifier = Modifier
                        .width(136.dp)
                        .height(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, outlineColor, RoundedCornerShape(16.dp))
                        .clickable { onPresetSelected(preset.key) }
                        .testTag("onboarding_theme_${preset.key}"),
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Palette preview indicator circles
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(preset.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(preset.secondary)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = preset.name,
                            color = if (isSelected) Color.White else AppTheme.colors.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStatesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Timeline,
            contentDescription = null,
            tint = AppTheme.colors.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Salah Consistency Levels",
            color = AppTheme.colors.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Accurate ledger entry allows the app to score your commitment. Build streaks to rank up from Seeker to Guardian.",
            color = AppTheme.colors.grayMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Explain state types
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StateExplainCard(
                icon = Icons.Default.CheckCircle,
                statusName = "PRAYED ON TIME",
                color = Color(0xFF10B981),
                desc = "Marked immediately within the prime calculated window. Best for streak building."
            )
            StateExplainCard(
                icon = Icons.Default.AccessTime,
                statusName = "PRAYED LATE",
                color = Color(0xFFFFB300),
                desc = "Completed outside the initial sweet spot. Ledger tracks both on-time and late counts."
            )
            StateExplainCard(
                icon = Icons.Default.Error,
                statusName = "MISSED (QADA/REWORK)",
                color = Color(0xFFFF3344),
                desc = "Missed entries are forged onto lists. Track and clear old tallies inside 'The Forge'."
            )
        }
    }
}

@Composable
fun StateExplainCard(
    icon: ImageVector,
    statusName: String,
    color: Color,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = statusName,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = desc,
                color = AppTheme.colors.grayMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun OnboardingLocationPage(
    selectedLocation: LocationPreset,
    onLocationSelected: (LocationPreset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = AppTheme.colors.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "YOUR PHYSICAL SECTOR",
            color = AppTheme.colors.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "Set Prayer Coordinates",
            color = AppTheme.colors.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Emad calculates cosmic astronomical sunset, dawn, and dusk angles exactly down to your city's GPS location. Default Aleppo pre-configured.",
            color = AppTheme.colors.grayMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Dropdown/Scrollable selector for presets
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(OnboardingLocationPresets) { preset ->
                    val isSelected = preset.name == selectedLocation.name
                    val bgColor = if (isSelected) AppTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .clickable { onLocationSelected(preset) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = preset.name,
                                color = if (isSelected) Color.White else AppTheme.colors.onBackground,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Lat: ${preset.latitude}, Lon: ${preset.longitude}",
                                color = AppTheme.colors.grayMuted,
                                fontSize = 11.sp
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingNotificationPage(
    notificationsEnabled: Boolean,
    onNotificationsToggled: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            tint = AppTheme.colors.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "STAY STEADFAST",
            color = AppTheme.colors.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "Salah Notification Alerts",
            color = AppTheme.colors.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Receiving prompt reminders at exact prayer moments simplifies consistency. Alarms are computed background tasks, run offline securely.",
            color = AppTheme.colors.grayMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prayer Alerts Engine",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Turn on active alarms for Fajr, Dhuhr, Asr, Maghrib, and Isha.",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { onNotificationsToggled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.primary,
                        checkedTrackColor = AppTheme.colors.primary.copy(alpha = 0.2f),
                        uncheckedThumbColor = AppTheme.colors.grayMuted,
                        uncheckedTrackColor = AppTheme.colors.surface
                    ),
                    modifier = Modifier.testTag("onboarding_notif_switch")
                )
            }
        }
    }
}
