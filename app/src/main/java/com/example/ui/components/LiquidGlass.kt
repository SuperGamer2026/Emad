package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.liquidGlassCard(
    shape: Shape = RoundedCornerShape(26.dp),
    accentColor: Color = Color.Transparent
): Modifier {
    return this
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color.White.copy(alpha = 0.005f)
                )
            )
        )
        .background(
            Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                radius = 600f
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.04f),
                    Color.Transparent,
                    accentColor.copy(alpha = 0.16f)
                ),
                start = Offset(0f, 0f),
                end = Offset(400f, 800f) // Sleek diagonal light reflection
            ),
            shape = shape
        )
}

fun Modifier.cosmicAuroraBackground(
    accentColor: Color,
    secondaryAccent: Color = Color.Transparent
): Modifier {
    return this.drawBehind {
        val w = size.width
        val h = size.height

        // Solid obsidian backdrop
        drawRect(color = Color(0xFF040307))

        // Protect against 0 size on initial measure which crashes radialGradient
        val r1 = (w * 1.4f).coerceAtLeast(1f)
        val r2 = (w * 0.95f).coerceAtLeast(1f)
        val r3 = (w * 1.15f).coerceAtLeast(1f)

        // Center deep spiritual purple fog
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0E0B19),
                    Color(0xFF040307)
                ),
                radius = r1
            ),
            radius = r1,
            center = Offset(w / 2f, h / 2f)
        )

        // Celestial Aurora Glow (Primary)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.16f),
                    accentColor.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                radius = r2
            ),
            radius = r2,
            center = Offset(w * 0.1f, h * 0.12f)
        )

        // Balancing Accent Glow (Secondary)
        val balanceColor = if (secondaryAccent != Color.Transparent) secondaryAccent else accentColor
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    balanceColor.copy(alpha = 0.13f),
                    balanceColor.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                radius = r3
            ),
            radius = r3,
            center = Offset(w * 0.9f, h * 0.85f)
        )
    }
}
