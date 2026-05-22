package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AppTheme
import com.example.ui.components.cosmicAuroraBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    appIconPreset: String,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Icon scaling & entry animation states
    val iconScale = remember { Animatable(0.2f) }
    val iconAlpha = remember { Animatable(0f) }
    val iconRotation = remember { Animatable(-10f) }

    // 2. Halo/Glow aura soft breathing state
    val haloScale = remember { Animatable(0.5f) }
    val haloAlpha = remember { Animatable(0f) }

    // 3. Text & subtitle animation states
    val textAlpha = remember { Animatable(0f) }
    val textYOffset = remember { Animatable(24f) }
    val subtitleAlpha = remember { Animatable(0f) }

    // 4. Overall overlay fade-out state
    val overallAlpha = remember { Animatable(1f) }

    // Identify corresponding launcher foreground icon asset depending on active user app icon preset
    val context = androidx.compose.ui.platform.LocalContext.current
    val iconResId = remember(appIconPreset) {
        val iconName = "ic_launcher_foreground_${if (appIconPreset == "BLOOD_MOON") "blood_moon" else appIconPreset.lowercase()}"
        val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
        if (resId != 0) resId else com.example.R.drawable.ic_launcher_foreground_expressive
    }

    LaunchedEffect(Unit) {
        // Step A: Trigger Icon & Halo Entry Animations
        coroutineScope.launch {
            // Soft tactile confirmation
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            
            launch {
                iconAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
                )
            }
            launch {
                iconRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            iconScale.animateTo(
                targetValue = 1.08f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            iconScale.animateTo(
                targetValue = 1.00f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        coroutineScope.launch {
            launch {
                haloAlpha.animateTo(
                    targetValue = 0.55f,
                    animationSpec = tween(durationMillis = 650, easing = EaseOutBack)
                )
                // Gentle pulse
                haloAlpha.animateTo(
                    targetValue = 0.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1800, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            haloScale.animateTo(
                targetValue = 1.25f,
                animationSpec = tween(durationMillis = 700, easing = EaseOutBack)
            )
            haloScale.animateTo(
                targetValue = 1.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        // Step B: Text Fade-in & Slide-up (Delayed by 250ms)
        delay(250)
        coroutineScope.launch {
            launch {
                textAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = EaseOutCubic)
                )
            }
            textYOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Step C: Subtitle Transition (Delayed by 450ms)
        delay(200)
        coroutineScope.launch {
            subtitleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = EaseOutSine)
            )
        }

        // Step D: Remain in finalized stunning beauty for 700ms
        delay(700)

        // Step E: Initiate Outro Smooth Fade-out Screen Transition
        overallAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)
        )

        // Wave off to show app main home/onboarding contents
        onSplashFinished()
    }

    // Full screen overlay with overall transition control
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(overallAlpha.value)
            .cosmicAuroraBackground(
                accentColor = AppTheme.colors.primary,
                secondaryAccent = AppTheme.colors.secondary
            )
            .background(Color(0xFF020104)), // Solid backing to prevent underlying pop frames
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo cluster with glowing radial backing
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Pulsing glowing background halo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(haloScale.value)
                        .alpha(haloAlpha.value)
                        .blur(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AppTheme.colors.primary,
                                    AppTheme.colors.secondary.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Themed Pillar Icon representation
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = "Emad Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .alpha(iconAlpha.value)
                        .scale(iconScale.value)
                        .graphicsLayer(rotationZ = iconRotation.value)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main branding text
            Text(
                text = "Emad",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.2.sp,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textYOffset.value.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle text representing our spiritual pillar core
            Text(
                text = "الصلاة عماد الدين",
                color = AppTheme.colors.primary.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .alpha(subtitleAlpha.value)
                    .graphicsLayer(translationY = (textYOffset.value * 0.5f))
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "The Pillar of Faith",
                color = AppTheme.colors.grayMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .alpha(subtitleAlpha.value)
                    .graphicsLayer(translationY = (textYOffset.value * 0.5f))
            )
        }
    }
}
