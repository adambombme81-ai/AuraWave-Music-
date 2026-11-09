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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.WallpaperPalette
import kotlin.math.sin

@Composable
fun SynthwaveWallpaper(
    palette: WallpaperPalette,
    bands: FloatArray,
    amplitude: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "synthwave")
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 1200 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridOffset"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val horizonY = height * 0.52f

        // 1. Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF070514),
                    palette.accent.copy(alpha = 0.4f),
                    palette.secondary.copy(alpha = 0.6f),
                    palette.primary.copy(alpha = 0.8f)
                ),
                startY = 0f,
                endY = horizonY
            ),
            size = Size(width, horizonY)
        )

        // 2. Stars
        drawSynthwaveStars(width, horizonY, amplitude)

        // 3. Glowing Synthwave Sun
        val sunRadius = (width * 0.22f) * (1f + amplitude * 0.15f)
        val sunCenter = Offset(width * 0.5f, horizonY - sunRadius * 0.35f)

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.secondary.copy(alpha = 0.6f + amplitude * 0.3f),
                    palette.primary.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = sunCenter,
                radius = sunRadius * 1.6f
            ),
            radius = sunRadius * 1.6f,
            center = sunCenter
        )

        // Sun body
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(palette.primary, palette.secondary, palette.accent),
                startY = sunCenter.y - sunRadius,
                endY = sunCenter.y + sunRadius
            ),
            radius = sunRadius,
            center = sunCenter
        )

        // Sun horizontal stripes (retro 80s cutout effect)
        val stripeCount = 7
        for (i in 0 until stripeCount) {
            val stripeHeight = 3f + (i * 3.5f)
            val yOffset = sunCenter.y + (i * (sunRadius / (stripeCount + 1)))
            drawRect(
                color = Color(0xFF0A071E),
                topLeft = Offset(sunCenter.x - sunRadius, yOffset),
                size = Size(sunRadius * 2, stripeHeight)
            )
        }

        // 4. Wireframe Mountains along horizon
        drawMountains(width, horizonY, palette, bands)

        // 5. Grid Floor (Space below horizon)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F0A28), Color(0xFF04020C)),
                startY = horizonY,
                endY = height
            ),
            topLeft = Offset(0f, horizonY),
            size = Size(width, height - horizonY)
        )

        // 3D Perspective Grid
        drawPerspectiveGrid(width, height, horizonY, gridOffset, palette, amplitude)

        // Audio reactive horizon beam
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    palette.primary.copy(alpha = 0.8f + amplitude * 0.2f),
                    Color.White,
                    palette.secondary.copy(alpha = 0.8f + amplitude * 0.2f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, horizonY),
            end = Offset(width, horizonY),
            strokeWidth = 3f + amplitude * 5f
        )
    }
}

private fun DrawScope.drawSynthwaveStars(width: Float, horizonY: Float, amplitude: Float) {
    val seedStars = listOf(
        0.1f to 0.15f, 0.25f to 0.08f, 0.4f to 0.22f, 0.6f to 0.12f,
        0.75f to 0.25f, 0.88f to 0.1f, 0.18f to 0.35f, 0.82f to 0.38f,
        0.32f to 0.42f, 0.68f to 0.45f, 0.5f to 0.06f, 0.93f to 0.28f
    )
    seedStars.forEach { (xRatio, yRatio) ->
        val x = width * xRatio
        val y = horizonY * yRatio
        val r = (1.5f + (xRatio * 2f)) * (1f + amplitude * 0.5f)
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = r,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawMountains(
    width: Float,
    horizonY: Float,
    palette: WallpaperPalette,
    bands: FloatArray
) {
    val path = Path()
    path.moveTo(0f, horizonY)

    val points = 16
    for (i in 0..points) {
        val x = (width / points) * i
        val bandIdx = (i * 2).coerceIn(0, bands.size - 1)
        val peakAmp = bands[bandIdx] * 40f
        val peakBase = sin(i.toDouble() * 0.8).toFloat() * 35f
        val y = horizonY - (peakBase.coerceAtLeast(0f) + peakAmp + 20f)
        path.lineTo(x, y)
    }
    path.lineTo(width, horizonY)
    path.close()

    drawPath(
        path = path,
        color = Color(0xFF140D30),
        style = Fill
    )

    drawPath(
        path = path,
        color = palette.secondary.copy(alpha = 0.5f),
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawPerspectiveGrid(
    width: Float,
    height: Float,
    horizonY: Float,
    gridOffset: Float,
    palette: WallpaperPalette,
    amplitude: Float
) {
    val floorHeight = height - horizonY
    val vanishX = width * 0.5f

    // Perspective lines radiating from horizon center
    val numRadialLines = 14
    for (i in -numRadialLines..numRadialLines) {
        val bottomX = vanishX + (i * (width / (numRadialLines * 0.8f)))
        drawLine(
            color = palette.primary.copy(alpha = 0.35f + amplitude * 0.2f),
            start = Offset(vanishX, horizonY),
            end = Offset(bottomX, height),
            strokeWidth = 1.5f
        )
    }

    // Horizontal moving grid lines with non-linear perspective spacing
    val numHorizLines = 10
    for (i in 0 until numHorizLines) {
        val progress = ((i.toFloat() + gridOffset) / numHorizLines) % 1.0f
        val yRatio = progress * progress // exponential perspective spacing
        val currentY = horizonY + (floorHeight * yRatio)
        val alpha = (progress * 0.7f + amplitude * 0.2f).coerceIn(0.1f, 0.9f)

        drawLine(
            color = palette.primary.copy(alpha = alpha),
            start = Offset(0f, currentY),
            end = Offset(width, currentY),
            strokeWidth = 1f + (progress * 2.5f)
        )
    }
}
