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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.WallpaperPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidLavaWallpaper(
    palette: WallpaperPalette,
    bands: FloatArray,
    amplitude: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid")
    val fluidPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 6000 else 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fluidPhase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width * 0.5f
        val cy = height * 0.5f

        // Dark deep base
        drawRect(Color(0xFF07060E))

        // Multiple Morphing Fluid Orbs
        val orbConfigs = listOf(
            Triple(0.35f, 0.35f, palette.primary),
            Triple(0.68f, 0.40f, palette.secondary),
            Triple(0.45f, 0.70f, palette.accent),
            Triple(0.75f, 0.75f, palette.primary),
            Triple(0.20f, 0.65f, palette.secondary)
        )

        orbConfigs.forEachIndexed { idx, (bx, by, orbColor) ->
            val phaseOffset = fluidPhase + (idx * 1.3f)
            val dx = cos(phaseOffset.toDouble()).toFloat() * (45f + amplitude * 35f)
            val dy = sin((phaseOffset * 1.4).toDouble()).toFloat() * (55f + amplitude * 40f)

            val ox = (width * bx) + dx
            val oy = (height * by) + dy
            val oRadius = (width * 0.42f) * (1f + amplitude * 0.25f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orbColor.copy(alpha = 0.55f + amplitude * 0.25f),
                        orbColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(ox, oy),
                    radius = oRadius
                ),
                radius = oRadius,
                center = Offset(ox, oy)
            )
        }

        // Audio Reactive Sine Ribbon Waves across screen
        val waveCount = 3
        for (w in 0 until waveCount) {
            val wavePath = Path()
            val waveY = cy - 80f + (w * 90f)
            val waveColor = if (w == 0) palette.primary else if (w == 1) palette.secondary else palette.accent

            wavePath.moveTo(0f, waveY)
            val segments = 20
            for (s in 0..segments) {
                val sx = (width / segments) * s
                val bandVal = bands[(s * bands.size / segments).coerceIn(0, bands.size - 1)]
                val waveAmp = (35f + bandVal * 45f + amplitude * 30f)
                val sy = waveY + sin(fluidPhase * 2 + (s * 0.4) + w).toFloat() * waveAmp
                wavePath.lineTo(sx, sy)
            }

            drawPath(
                path = wavePath,
                color = waveColor.copy(alpha = 0.4f + amplitude * 0.3f),
                style = Stroke(width = 3f + (w * 1.5f))
            )
        }
    }
}
