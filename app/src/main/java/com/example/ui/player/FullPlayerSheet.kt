package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MusicSource
import com.example.model.RepeatMode
import com.example.model.WallpaperPalette
import com.example.model.WallpaperType
import com.example.player.UnifiedPlayerManager
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.YouTubeRed
import com.example.ui.wallpapers.LiveWallpaperCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    playerManager: UnifiedPlayerManager,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentTrack by playerManager.currentTrack.collectAsState()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val positionMs by playerManager.currentPositionMs.collectAsState()
    val durationMs by playerManager.durationMs.collectAsState()
    val isShuffle by playerManager.isShuffle.collectAsState()
    val repeatMode by playerManager.repeatMode.collectAsState()

    val wallpaperType by playerManager.wallpaperType.collectAsState()
    val wallpaperPalette by playerManager.wallpaperPalette.collectAsState()
    val bands by playerManager.visualizerBands.collectAsState()
    val amplitude by playerManager.currentAmplitude.collectAsState()
    val showVideo by playerManager.showVideoInsteadOfWallpaper.collectAsState()
    val isImmersive by playerManager.isImmersiveWallpaperMode.collectAsState()

    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showPalettePicker by remember { mutableStateOf(false) }

    val track = currentTrack ?: return

    val sourceBadgeColor = when (track.source) {
        MusicSource.YOUTUBE -> YouTubeRed
        MusicSource.SPOTIFY -> SpotifyGreen
        MusicSource.LOCAL -> Color(0xFF3A86FF)
        MusicSource.CURATED -> Color(0xFF9D4EDD)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090812))
            .testTag("full_player_screen")
    ) {
        // Background / Visual Area: Either YouTube Video OR Audio-Reactive Live Wallpaper
        if (track.source == MusicSource.YOUTUBE && showVideo) {
            YouTubePlayerView(
                videoId = track.sourceUri,
                playerManager = playerManager,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LiveWallpaperCanvas(
                wallpaperType = wallpaperType,
                palette = wallpaperPalette,
                bands = bands,
                amplitude = amplitude,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize(),
                onTap = {
                    playerManager.toggleImmersiveWallpaperMode()
                }
            )
        }

        // Immersive Mode Floating Mini-Dock
        AnimatedVisibility(
            visible = isImmersive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = DarkSurfaceElevated.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.width(140.dp)) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = wallpaperType.title,
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { playerManager.togglePlayPause() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = NeonCyan
                        )
                    }

                    IconButton(
                        onClick = { playerManager.next() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { playerManager.setImmersiveWallpaperMode(false) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Immersive",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Full UI Controls Layer (visible when NOT in immersive mode)
        AnimatedVisibility(
            visible = !isImmersive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xDD090812),
                                Color(0x44090812),
                                Color(0xF0090812)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("full_player_collapse_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Source Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(sourceBadgeColor.copy(alpha = 0.25f))
                            .border(1.dp, sourceBadgeColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = track.source.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = sourceBadgeColor
                        )
                    }

                    Row {
                        // Toggle video if YouTube
                        if (track.source == MusicSource.YOUTUBE) {
                            IconButton(
                                onClick = { playerManager.toggleShowVideo() },
                                modifier = Modifier.testTag("toggle_video_button")
                            ) {
                                Icon(
                                    imageVector = if (showVideo) Icons.Default.Wallpaper else Icons.Default.SmartDisplay,
                                    contentDescription = if (showVideo) "Show Wallpaper" else "Show Video",
                                    tint = if (showVideo) YouTubeRed else NeonCyan
                                )
                            }
                        }

                        // Fullscreen immersive visualizer mode
                        IconButton(
                            onClick = { playerManager.setImmersiveWallpaperMode(true) },
                            modifier = Modifier.testTag("immersive_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Full Screen Visualizer",
                                tint = NeonCyan
                            )
                        }

                        // Equalizer
                        IconButton(
                            onClick = { showEqualizerSheet = true },
                            modifier = Modifier.testTag("open_equalizer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Equalizer",
                                tint = Color.White
                            )
                        }

                        // Sleep Timer
                        IconButton(
                            onClick = { showSleepTimerSheet = true },
                            modifier = Modifier.testTag("open_sleep_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Sleep Timer",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Track Title & Heart
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track.artist + if (track.album.isNotBlank()) " • ${track.album}" else "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.75f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.testTag("toggle_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) NeonPink else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Slider
                    val progress = if (durationMs > 0) {
                        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Slider(
                        value = progress,
                        onValueChange = { newProg ->
                            playerManager.seekTo((newProg * durationMs).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_progress_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(positionMs),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatTime(durationMs),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconButton(
                            onClick = { playerManager.toggleShuffle() },
                            modifier = Modifier.testTag("player_shuffle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffle) NeonCyan else Color.White.copy(alpha = 0.5f)
                            )
                        }

                        // Previous
                        IconButton(
                            onClick = { playerManager.previous() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_previous_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Play / Pause Large Glow Button
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(16.dp, CircleShape, spotColor = NeonCyan)
                                .clip(CircleShape)
                                .background(NeonCyan)
                                .clickable { playerManager.togglePlayPause() }
                                .testTag("player_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF090812),
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        // Next
                        IconButton(
                            onClick = { playerManager.next() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Repeat
                        IconButton(
                            onClick = { playerManager.toggleRepeat() },
                            modifier = Modifier.testTag("player_repeat_button")
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    RepeatMode.ONE -> Icons.Default.RepeatOne
                                    else -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = if (repeatMode != RepeatMode.OFF) NeonCyan else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Wallpaper Style Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LIVE WALLPAPER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )

                        // Palette button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .clickable { showPalettePicker = !showPalettePicker }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Palette",
                                tint = wallpaperPalette.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = wallpaperPalette.title,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Wallpaper Types horizontal scroll
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WallpaperType.entries.forEach { wpType ->
                            val isSel = wpType == wallpaperType
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSel) NeonCyan.copy(alpha = 0.25f) else DarkSurfaceElevated
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) NeonCyan else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { playerManager.setWallpaperType(wpType) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("wallpaper_chip_${wpType.name.lowercase()}")
                            ) {
                                Text(
                                    text = wpType.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) NeonCyan else Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Optional Palette Picker Row
                    if (showPalettePicker) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WallpaperPalette.entries.forEach { palette ->
                                val isPalSel = palette == wallpaperPalette
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(palette.primary.copy(alpha = if (isPalSel) 0.35f else 0.15f))
                                        .border(
                                            1.dp,
                                            if (isPalSel) palette.primary else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { playerManager.setWallpaperPalette(palette) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = palette.title,
                                        fontSize = 11.sp,
                                        color = if (isPalSel) palette.primary else Color.White.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEqualizerSheet) {
        EqualizerSheet(
            playerManager = playerManager,
            onDismiss = { showEqualizerSheet = false }
        )
    }

    if (showSleepTimerSheet) {
        SleepTimerSheet(
            playerManager = playerManager,
            onDismiss = { showSleepTimerSheet = false }
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
