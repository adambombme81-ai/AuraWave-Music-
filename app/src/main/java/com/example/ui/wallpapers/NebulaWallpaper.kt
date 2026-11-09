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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.WallpaperPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NebulaWallpaper(
    palette: WallpaperPalette,
    bands: FloatArray,
    amplitude: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nebula")
    val cosmicTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 2000 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cosmicTime"
    )

    val nebulaDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nebulaDrift"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width * 0.5f
        val cy = height * 0.5f

        // Deep cosmos space backdrop
        drawRect(Color(0xFF05030A))

        // Interstellar Nebula clouds (multiple radial gradients)
        val rad1 = Math.toRadians(nebulaDrift.toDouble())
        val nebula1Center = Offset(
            cx + cos(rad1).toFloat() * 60f,
            cy + sin(rad1).toFloat() * 80f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.secondary.copy(alpha = 0.45f + amplitude * 0.2f),
                    palette.accent.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = nebula1Center,
                radius = width * 0.65f
            ),
            radius = width * 0.65f,
            center = nebula1Center
        )

        val rad2 = Math.toRadians((nebulaDrift + 140).toDouble())
        val nebula2Center = Offset(
            cx + cos(rad2).toFloat() * 90f,
            cy + sin(rad2).toFloat() * 50f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.primary.copy(alpha = 0.4f + amplitude * 0.25f),
                    palette.secondary.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = nebula2Center,
                radius = width * 0.55f
            ),
            radius = width * 0.55f,
            center = nebula2Center
        )

        // Hyperspace Warp Stars radiating outward
        val starCount = 64
        for (i in 0 until starCount) {
            val starSeed = (i * 37.13f) % 1.0f
            val starAngle = (i * (360f / starCount) + (starSeed * 45f))
            val angleRad = Math.toRadians(starAngle.toDouble())

            val offsetProgress = ((cosmicTime + starSeed) % 1.0f)
            // Quadratic acceleration away from center
            val dist = (offsetProgress * offsetProgress) * (width * 0.75f)

            val px = cx + cos(angleRad).toFloat() * dist
            val py = cy + sin(angleRad).toFloat() * dist

            // Star trail length proportional to speed & amplitude
            val trailDist = (offsetProgress * 24f * (1f + amplitude * 0.8f)).coerceAtLeast(2f)
            val trailX = px - cos(angleRad).toFloat() * trailDist
            val trailY = py - sin(angleRad).toFloat() * trailDist

            val starAlpha = (offsetProgress * (0.8f + amplitude * 0.2f)).coerceIn(0.1f, 1f)
            val starColor = if (i % 3 == 0) palette.primary else if (i % 3 == 1) palette.secondary else Color.White

            drawLine(
                color = starColor.copy(alpha = starAlpha),
                start = Offset(trailX, trailY),
                end = Offset(px, py),
                strokeWidth = 1.2f + (offsetProgress * 2.8f)
            )

            drawCircle(
                color = Color.White.copy(alpha = starAlpha),
                radius = 1f + (offsetProgress * 2.5f),
                center = Offset(px, py)
            )
        }

        // Center Pulsing Core Singularity
        val coreRadius = (width * 0.08f) * (1f + amplitude * 0.4f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    palette.primary.copy(alpha = 0.8f),
                    palette.secondary.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = coreRadius * 2f
            ),
            radius = coreRadius * 2f,
            center = Offset(cx, cy)
        )
    }
}
