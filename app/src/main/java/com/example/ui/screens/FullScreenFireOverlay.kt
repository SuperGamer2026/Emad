package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun FullScreenFireOverlay(
    isVisible: Boolean,
    onFinished: () -> Unit
) {
    if (!isVisible) return

    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val burnInAnim = remember { Animatable(0f) }
    val extinguishAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            var elapsed = 0L
            while (isActive && elapsed < 7000L) {
                val scale = burnInAnim.value * extinguishAnim.value
                if (scale > 0.05f) {
                    val amplitude = (scale * 80).toInt().coerceIn(1, 255) // max amplitude 80 is smooth
                    val duration = max(50, (scale * 80).toInt()).toLong()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            if (vibrator.hasAmplitudeControl()) {
                                vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                            } else {
                                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(duration)
                    }
                }
                val waitTime = max(50, (100 - scale * 50).toInt()).toLong()
                delay(waitTime)
                elapsed += waitTime
            }
        }

        burnInAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
        )
        delay(2000)
        extinguishAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        val primary = AppTheme.colors.primary
        val secondary = AppTheme.colors.secondary
        val infiniteTransition = rememberInfiniteTransition(label = "fullscreen_fire")

        val flicker1 by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flicker1"
        )
        
        val flicker2 by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flicker2"
        )

        val wanderX1 by infiniteTransition.animateFloat(
            initialValue = -30f,
            targetValue = 30f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wanderX1"
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val bottomY = height * 1.05f 

            val scale = burnInAnim.value * extinguishAnim.value
            
            // Allow the fire to be very wide and very tall
            val maxW = width * 1.8f
            val maxH = height * 0.95f

            // --- Outer Flame ---
            val outerPath = Path().apply {
                val outerW = maxW * 0.6f * flicker1 * scale
                val outerH = maxH * 0.85f * flicker2 * scale
                moveTo(centerX, bottomY - outerH)
                cubicTo(
                    centerX + outerW * 0.5f + wanderX1 * scale, bottomY - outerH * 0.6f,
                    centerX + outerW * 0.7f, bottomY,
                    centerX, bottomY
                )
                cubicTo(
                    centerX - outerW * 0.7f, bottomY,
                    centerX - outerW * 0.5f + wanderX1 * scale, bottomY - outerH * 0.6f,
                    centerX, bottomY - outerH
                )
                close()
            }
            
            drawPath(
                path = outerPath,
                brush = Brush.verticalGradient(
                    colors = listOf(secondary.copy(alpha = 0.2f), primary.copy(alpha = 0.9f)),
                    startY = bottomY - maxH * 0.85f * scale,
                    endY = bottomY
                )
            )

            // --- Middle Flame ---
            val middlePath = Path().apply {
                val middleW = maxW * 0.45f * flicker2 * scale
                val middleH = maxH * 0.6f * flicker1 * scale
                moveTo(centerX, bottomY - middleH)
                cubicTo(
                    centerX + middleW * 0.5f - wanderX1 * 0.8f * scale, bottomY - middleH * 0.6f,
                    centerX + middleW * 0.65f, bottomY,
                    centerX, bottomY
                )
                cubicTo(
                    centerX - middleW * 0.65f, bottomY,
                    centerX - middleW * 0.5f - wanderX1 * 0.8f * scale, bottomY - middleH * 0.6f,
                    centerX, bottomY - middleH
                )
                close()
            }
            
            drawPath(
                path = middlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(primary, secondary.copy(alpha = 0.95f)),
                    startY = bottomY - maxH * 0.6f * scale,
                    endY = bottomY
                )
            )

            // --- Inner Flame (Hot Core) ---
            val innerPath = Path().apply {
                val innerW = maxW * 0.28f * flicker1 * scale
                val innerH = maxH * 0.4f * flicker2 * scale
                moveTo(centerX, bottomY - innerH)
                cubicTo(
                    centerX + innerW * 0.5f + wanderX1 * 0.4f * scale, bottomY - innerH * 0.6f,
                    centerX + innerW * 0.6f, bottomY,
                    centerX, bottomY
                )
                cubicTo(
                    centerX - innerW * 0.6f, bottomY,
                    centerX - innerW * 0.5f + wanderX1 * 0.4f * scale, bottomY - innerH * 0.6f,
                    centerX, bottomY - innerH
                )
                close()
            }
            
            drawPath(
                path = innerPath,
                brush = Brush.verticalGradient(
                    colors = listOf(secondary, Color.White),
                    startY = bottomY - maxH * 0.4f * scale,
                    endY = bottomY
                )
            )
        }
    }
}
