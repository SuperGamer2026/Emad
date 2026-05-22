package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.components.cosmicAuroraBackground
import com.example.ui.components.liquidGlassCard
import com.example.ui.viewmodel.PrayerViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring

class MainActivity : ComponentActivity() {

    private val viewModel: PrayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isOnboardingCompleted by viewModel.onboardingCompleted.collectAsState()
            val themePreset by viewModel.themePreset.collectAsState()
            val appIconPreset by viewModel.appIconPreset.collectAsState()

            MyApplicationTheme(themePreset = themePreset) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        appIconPreset = appIconPreset,
                        onSplashFinished = { showSplash = false }
                    )
                } else if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onFinished = { preset, location, notificationsEnabled ->
                            viewModel.setOnboardingCompleted(
                                completed = true,
                                initialTheme = preset,
                                initialLocName = location.name,
                                lat = location.latitude,
                                lon = location.longitude,
                                tz = location.timezone,
                                notificationsEnabled = notificationsEnabled
                            )
                        },
                        onThemePreview = { preset ->
                            viewModel.themePreset.value = preset
                        },
                        onThemeConfirmed = { preset ->
                            viewModel.setThemePreset(preset)
                        }
                    )
                } else {
                    val initialRoute = intent.getStringExtra("START_DESTINATION") ?: "home"
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: initialRoute

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .cosmicAuroraBackground(
                                accentColor = AppTheme.colors.primary,
                                secondaryAccent = AppTheme.colors.secondary
                            ),
                        containerColor = Color.Transparent
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = initialRoute,
                                modifier = Modifier.fillMaxSize(),
                                enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) },
                                exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) },
                                popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) },
                                popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) }
                            ) {
                                composable("home") {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onTierClick = { navController.navigate("badges") }
                                    )
                                }
                                composable("badges") {
                                    BadgesScreen(
                                        viewModel = viewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                composable("forge") {
                                    QadaForgeScreen(viewModel = viewModel)
                                }
                                composable("compass") {
                                    QiblaCompassScreen(viewModel = viewModel)
                                }
                                composable("finetune") {
                                    FineTuneScreen(viewModel = viewModel)
                                }
                            }

                            // FLOATING iOS LIQUID GLASS DOCK OVERLAY
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp)
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .background(Color(0xFF040307).copy(alpha = 0.94f), RoundedCornerShape(32.dp))
                                    .liquidGlassCard(RoundedCornerShape(32.dp), AppTheme.colors.primary)
                                    .testTag("app_bottom_nav"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FloatingDockItem(
                                        selected = currentRoute == "home",
                                        onClick = {
                                            if (currentRoute != "home") {
                                                navController.navigate("home") {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = Icons.AutoMirrored.Outlined.List,
                                        selectedIcon = Icons.AutoMirrored.Filled.List,
                                        label = "Ledger",
                                        accentColor = AppTheme.colors.primary,
                                        modifier = Modifier.testTag("nav_home")
                                    )

                                    FloatingDockItem(
                                        selected = currentRoute == "forge",
                                        onClick = {
                                            if (currentRoute != "forge") {
                                                navController.navigate("forge") {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = Icons.Outlined.Build,
                                        selectedIcon = Icons.Default.Build,
                                        label = "The Forge",
                                        accentColor = AppTheme.colors.secondary,
                                        modifier = Modifier.testTag("nav_forge")
                                    )

                                    FloatingDockItem(
                                        selected = currentRoute == "compass",
                                        onClick = {
                                            if (currentRoute != "compass") {
                                                navController.navigate("compass") {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = Icons.Outlined.Explore,
                                        selectedIcon = Icons.Default.Explore,
                                        label = "Qibla",
                                        accentColor = AppTheme.colors.primary,
                                        modifier = Modifier.testTag("nav_compass")
                                    )

                                    FloatingDockItem(
                                        selected = currentRoute == "finetune",
                                        onClick = {
                                            if (currentRoute != "finetune") {
                                                navController.navigate("finetune") {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = Icons.Outlined.Settings,
                                        selectedIcon = Icons.Default.Settings,
                                        label = "Settings",
                                        accentColor = AppTheme.colors.secondary,
                                        modifier = Modifier.testTag("nav_finetune")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.FloatingDockItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.18f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)
    )

    val iconColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) accentColor else Color.White.copy(alpha = 0.45f),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)
    )

    val textColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) Color.White else Color.White.copy(alpha = 0.4f),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)
    )

    Column(
        modifier = modifier
            .weight(1.0f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        )
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}
