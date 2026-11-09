package com.example.ui.wallpapers

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.WallpaperPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EqualizerWallpaper(
    palette: WallpaperPalette,
    bands: FloatArray,
    amplitude: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 16000 else 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width * 0.5f
        val centerY = height * 0.48f

        // Dark background with ambient radial pulse
        drawRect(Color(0xFF090814))

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.primary.copy(alpha = 0.25f + amplitude * 0.25f),
                    palette.secondary.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = width * 0.65f
            ),
            center = Offset(centerX, centerY),
            radius = width * 0.65f
        )

        // Circular central pulsing aura rings
        val baseRadius = width * 0.22f
        val numRings = 3
        for (i in 0 until numRings) {
            val ringRadius = baseRadius + (i * 28f) + (amplitude * 35f)
            drawCircle(
                color = if (i % 2 == 0) palette.primary.copy(alpha = 0.3f) else palette.secondary.copy(alpha = 0.25f),
                radius = ringRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f + amplitude * 2f)
            )
        }

        // Orbital particle dots revolving around center
        val numOrbitals = 16
        for (i in 0 until numOrbitals) {
            val angleRad = Math.toRadians((rotation + (i * (360f / numOrbitals))).toDouble())
            val orbitalDist = baseRadius * 1.35f + (sin(angleRad * 3).toFloat() * 18f)
            val px = centerX + cos(angleRad).toFloat() * orbitalDist
            val py = centerY + sin(angleRad).toFloat() * orbitalDist
            val dotAlpha = (0.4f + amplitude * 0.5f).coerceIn(0.2f, 1f)

            drawCircle(
                color = palette.accent.copy(alpha = dotAlpha),
                radius = 3.5f + amplitude * 3f,
                center = Offset(px, py)
            )
        }

        // Spectrum bars (Mirrored layout bottom section)
        val numBars = 24
        val totalSpacing = width * 0.88f
        val barWidth = totalSpacing / (numBars * 1.4f)
        val barGap = barWidth * 0.4f
        val startX = (width - totalSpacing) * 0.5f
        val groundY = height * 0.85f

        for (i in 0 until numBars) {
            val bandIdx = (i * bands.size / numBars).coerceIn(0, bands.size - 1)
            val barAmp = bands[bandIdx]
            val barHeight = (barAmp * (height * 0.32f)).coerceAtLeast(8f)

            val x = startX + i * (barWidth + barGap)
            val topY = groundY - barHeight

            // Gradient filled bar
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.primary, palette.secondary, palette.accent),
                    startY = topY,
                    endY = groundY
                ),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth * 0.3f, barWidth * 0.3f)
            )

            // Floating cap above the bar
            val capY = topY - 8f - (barAmp * 6f)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.9f),
                topLeft = Offset(x, capY),
                size = Size(barWidth, 3f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }

        // Reflection below ground line
        for (i in 0 until numBars) {
            val bandIdx = (i * bands.size / numBars).coerceIn(0, bands.size - 1)
            val barAmp = bands[bandIdx]
            val reflectHeight = (barAmp * (height * 0.12f)).coerceAtLeast(4f)
            val x = startX + i * (barWidth + barGap)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    startY = groundY + 4f,
                    endY = groundY + 4f + reflectHeight
                ),
                topLeft = Offset(x, groundY + 4f),
                size = Size(barWidth, reflectHeight),
                cornerRadius = CornerRadius(barWidth * 0.2f, barWidth * 0.2f)
            )
        }
    }
}
