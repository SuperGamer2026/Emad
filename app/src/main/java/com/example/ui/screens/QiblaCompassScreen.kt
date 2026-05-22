package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.components.liquidGlassCard
import com.example.ui.viewmodel.PrayerViewModel
import androidx.compose.ui.draw.drawBehind
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun QiblaCompassScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val activeLat by viewModel.latitude.collectAsState()
    val activeLon by viewModel.longitude.collectAsState()
    val locationName by viewModel.locationName.collectAsState()

    // Coordinates of Mecca
    val meccaLat = 21.4225
    val meccaLon = 39.8262

    val qiblaBearingValue = remember(activeLat, activeLon) {
        val latR = Math.toRadians(activeLat)
        val latM = Math.toRadians(meccaLat)
        val deltaLon = Math.toRadians(meccaLon - activeLon)
        val y = sin(deltaLon)
        val x = cos(latR) * sin(latM) - sin(latR) * cos(latM) * cos(deltaLon)
        var b = Math.toDegrees(atan2(y, x))
        ((b + 360.0) % 360.0).toFloat()
    }

    // Sensory direction states
    var azimuth by remember { mutableStateOf(0f) }
    var simulationMode by remember { mutableStateOf(false) }
    var simulatedAzimuth by remember { mutableStateOf(120f) } // default starting at 120° for exploration

    val currentSensorAzimuth = remember { mutableStateOf(0f) }

    // Setup sensor listener
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientationValues = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationValues)
                    val azimuthRad = orientationValues[0]
                    val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                    val normalized = (azimuthDeg + 360f) % 360f
                    currentSensorAzimuth.value = normalized
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // React to either actual or simulation bearing changes
    val displayedAzimuth = if (simulationMode) simulatedAzimuth else azimuth

    LaunchedEffect(currentSensorAzimuth.value) {
        if (!simulationMode) {
            azimuth = currentSensorAzimuth.value
        }
    }

    // Precise relative Mecca (Qibla) indicator direction relative to device top
    val relativeQiblaAngle = (qiblaBearingValue - displayedAzimuth + 360f) % 360f

    // Check if perfectly aligned to Mecca (+/- 5 degrees)
    val isAligned = abs(relativeQiblaAngle) <= 5f || abs(relativeQiblaAngle - 360f) <= 5f

    // Soft haptic tick when it aligns perfectly
    LaunchedEffect(isAligned) {
        if (isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(300)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val glowColor = if (isAligned) AppTheme.colors.primary else AppTheme.colors.secondary.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen title
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SENSOR-DRIVEN COMPASS",
                color = AppTheme.colors.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Precise Qibla Finder",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Calculated for $locationName: ${String.format("%.1f°", qiblaBearingValue)} E of N",
                color = AppTheme.colors.grayMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }

        // Animated alignment status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(RoundedCornerShape(16.dp), if (isAligned) AppTheme.colors.primary else Color.Transparent)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "Calibration Status",
                    tint = if (isAligned) AppTheme.colors.primary else AppTheme.colors.secondary
                )
                Text(
                    text = if (isAligned) "QIBLA ALIGNED" else "STEER PHONE TO ${String.format("%.1f°", qiblaBearingValue)}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Main Compass Dial Container
        Box(
            modifier = Modifier
                .size(280.dp)
                .liquidGlassCard(CircleShape, glowColor)
                .drawBehind {
                    val canvasSize = this.size
                    drawCircle(
                        color = glowColor.copy(alpha = if (isAligned) 0.1f else 0.02f),
                        radius = canvasSize.minDimension / 2
                    )
                }
                .pointerInput(simulationMode) {
                    if (simulationMode) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            simulatedAzimuth = (simulatedAzimuth - (dragAmount.x * 0.5f) + 360f) % 360f
                        }
                    }
                }
                .testTag("compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Compass Ring
            val ringPrimaryColor = AppTheme.colors.primary
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .rotate(-displayedAzimuth),
                contentAlignment = Alignment.Center
            ) {
                // Drawing the cardinal directions & ticks
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Draw outer dashed tick circle
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = r - 10f,
                        style = Stroke(width = 2f)
                    )

                    // Draw major 4 ticks (N, S, E, W)
                    val directions = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                    directions.forEach { (label, angle) ->
                        val angleRad = Math.toRadians(angle.toDouble())
                        val tickStart = Offset(
                            (center.x + (r - 28f) * sin(angleRad)).toFloat(),
                            (center.y - (r - 28f) * cos(angleRad)).toFloat()
                        )
                        val tickEnd = Offset(
                            (center.x + (r - 12f) * sin(angleRad)).toFloat(),
                            (center.y - (r - 12f) * cos(angleRad)).toFloat()
                        )
                        drawLine(
                            color = if (label == "N") ringPrimaryColor else Color.White.copy(alpha = 0.3f),
                            start = tickStart,
                            end = tickEnd,
                            strokeWidth = if (label == "N") 6f else 4f
                        )
                    }
                }

                // Inner rotating arrow
                Text(
                    text = "N",
                    color = AppTheme.colors.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
                )
                Text(
                    text = "S",
                    color = AppTheme.colors.onBackground.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)
                )
                Text(
                    text = "E",
                    color = AppTheme.colors.onBackground.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp)
                )
                Text(
                    text = "W",
                    color = AppTheme.colors.onBackground.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp)
                )
            }

            // Qibla Needle pointing relative to Top
            val needleSecondaryColor = AppTheme.colors.secondary
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .rotate(relativeQiblaAngle),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val r = size.minDimension / 2

                    val path = Path().apply {
                        moveTo(center.x, center.y - (r - 35f))
                        lineTo(center.x - 12f, center.y - 100f)
                        lineTo(center.x - 4f, center.y - 45f)
                        lineTo(center.x + 4f, center.y - 45f)
                        lineTo(center.x + 12f, center.y - 100f)
                        close()
                    }

                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            listOf(glowColor, glowColor.copy(alpha = 0.3f))
                        )
                    )

                    drawCircle(
                        color = needleSecondaryColor,
                        radius = 12f,
                        center = Offset(center.x, center.y - (r - 18f))
                    )
                }
            }

            // Central glass core indicator
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surface)
                    .border(2.dp, glowColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MECCA",
                        color = if (isAligned) AppTheme.colors.primary else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${displayedAzimuth.roundToInt()}°",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Simulation toggler block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(RoundedCornerShape(16.dp), AppTheme.colors.secondary)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SIMULATOR SYSTEM",
                        color = AppTheme.colors.secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Touch/Drag Simulation",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = simulationMode,
                    onCheckedChange = {
                        simulationMode = it
                        if (it) {
                            simulatedAzimuth = 120f
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.primary,
                        checkedTrackColor = AppTheme.colors.primary.copy(alpha = 0.2f),
                        uncheckedThumbColor = AppTheme.colors.grayMuted,
                        uncheckedTrackColor = AppTheme.colors.surface
                    ),
                    modifier = Modifier.testTag("sensor_simulation_switch")
                )
            }

            AnimatedVisibility(visible = simulationMode) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Text(
                        text = "Drag or swipe across the compass dial above to rotate the phone manually and align with Mecca (~157°).",
                        color = AppTheme.colors.grayMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SIMULATED PHONE AZIMUTH: ${simulatedAzimuth.roundToInt()}°",
                        color = AppTheme.colors.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
