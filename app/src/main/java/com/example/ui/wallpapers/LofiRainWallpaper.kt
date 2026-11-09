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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.WallpaperPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LofiRainWallpaper(
    palette: WallpaperPalette,
    bands: FloatArray,
    amplitude: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lofi")

    // Vinyl rotation
    val vinylAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 2800 else 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylAngle"
    )

    // Rain drop descent
    val rainPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 1500 else 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainPhase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width * 0.5f
        val cy = height * 0.48f

        // Cozy moody window glass background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0D0B16),
                    Color(0xFF141026),
                    Color(0xFF0A0714)
                )
            )
        )

        // Warm bokeh blur circles (city lights seen through rainy window)
        val bokehList = listOf(
            Triple(0.2f, 0.25f, 50f),
            Triple(0.8f, 0.2f, 65f),
            Triple(0.35f, 0.75f, 80f),
            Triple(0.75f, 0.7f, 55f),
            Triple(0.15f, 0.65f, 45f),
            Triple(0.85f, 0.45f, 70f)
        )
        bokehList.forEachIndexed { i, (bx, by, bRadius) ->
            val pulse = sin((vinylAngle * 0.05 + i).toDouble()).toFloat() * 0.15f
            val baseColor = if (i % 2 == 0) palette.primary else palette.secondary
            drawCircle(
                color = baseColor.copy(alpha = (0.18f + pulse + amplitude * 0.1f).coerceIn(0.08f, 0.4f)),
                radius = bRadius * (1f + amplitude * 0.12f),
                center = Offset(width * bx, height * by)
            )
        }

        // Center Spinning Vinyl Record
        val recordRadius = width * 0.36f
        rotate(degrees = vinylAngle, pivot = Offset(cx, cy)) {
            // Vinyl outer shadow & rim
            drawCircle(
                color = Color(0xFF0F0E17),
                radius = recordRadius,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFF1B1926),
                radius = recordRadius * 0.98f,
                center = Offset(cx, cy)
            )

            // Vinyl groove lines
            val grooveCount = 14
            for (g in 0 until grooveCount) {
                val gRadius = recordRadius * (0.42f + (g * 0.04f))
                drawCircle(
                    color = Color(0xFF28253B).copy(alpha = 0.5f),
                    radius = gRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1f)
                )
            }

            // Reflective light sheen on grooves
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.12f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy)
                ),
                radius = recordRadius * 0.95f,
                center = Offset(cx, cy)
            )

            // Center Label (Album Art disc)
            val labelRadius = recordRadius * 0.38f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.primary, palette.secondary, palette.accent),
                    center = Offset(cx, cy),
                    radius = labelRadius
                ),
                radius = labelRadius,
                center = Offset(cx, cy)
            )

            // Center Spindle Hole
            drawCircle(
                color = Color(0xFF0A0812),
                radius = recordRadius * 0.08f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = recordRadius * 0.08f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
        }

        // Tonearm drawing (stationary with gentle audio vibration)
        val armPivot = Offset(width * 0.88f, height * 0.22f)
        val armHead = Offset(cx + recordRadius * 0.5f + (sin(vinylAngle * 0.1).toFloat() * 3f), cy - recordRadius * 0.2f)
        drawLine(
            color = Color(0xFFCCCCCC),
            start = armPivot,
            end = armHead,
            strokeWidth = 4f
        )
        drawCircle(
            color = palette.accent,
            radius = 6f,
            center = armPivot
        )
        // Cartridge / Stylus
        drawRect(
            color = Color(0xFF222222),
            topLeft = Offset(armHead.x - 6f, armHead.y - 6f),
            size = Size(16f, 12f)
        )

        // Raindrops streaming down the glass
        val rainCount = 45
        for (i in 0 until rainCount) {
            val rxSeed = (i * 19.3f) % 1.0f
            val dropProgress = ((rainPhase + rxSeed) % 1.0f)
            val rx = width * rxSeed
            val ry = height * dropProgress

            val dropLength = 18f + (rxSeed * 22f)
            val dropAlpha = (0.25f + (rxSeed * 0.5f) + amplitude * 0.2f).coerceIn(0.2f, 0.85f)

            drawLine(
                color = Color(0xFFC0D6DF).copy(alpha = dropAlpha),
                start = Offset(rx, ry),
                end = Offset(rx - 4f, ry + dropLength),
                strokeWidth = 1.5f
            )
        }
    }
}
