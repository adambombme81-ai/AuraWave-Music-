package com.example.ui.wallpapers

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.model.WallpaperPalette
import com.example.model.WallpaperType

@Composable
fun LiveWallpaperCanvas(
    wallpaperType: WallpaperType,
    palette: WallpaperPalette,
    bands: FloatArray,
    amplitude: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .testTag("live_wallpaper_canvas")
            .then(
                if (onTap != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onTap()
                    }
                } else Modifier
            )
    ) {
        Crossfade(
            targetState = wallpaperType,
            animationSpec = tween(500),
            label = "wallpaper_crossfade"
        ) { type ->
            when (type) {
                WallpaperType.SYNTHWAVE -> SynthwaveWallpaper(
                    palette = palette,
                    bands = bands,
                    amplitude = amplitude,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
                WallpaperType.CYBERPUNK -> EqualizerWallpaper(
                    palette = palette,
                    bands = bands,
                    amplitude = amplitude,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
                WallpaperType.NEBULA -> NebulaWallpaper(
                    palette = palette,
                    bands = bands,
                    amplitude = amplitude,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
                WallpaperType.LOFI_RAIN -> LofiRainWallpaper(
                    palette = palette,
                    bands = bands,
                    amplitude = amplitude,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
                WallpaperType.LIQUID_LAVA -> LiquidLavaWallpaper(
                    palette = palette,
                    bands = bands,
                    amplitude = amplitude,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
