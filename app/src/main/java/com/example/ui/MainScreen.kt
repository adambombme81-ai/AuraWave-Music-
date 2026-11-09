package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MusicRepository
import com.example.player.UnifiedPlayerManager
import com.example.ui.player.FullPlayerSheet
import com.example.ui.player.MiniPlayerBar
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocalMusicScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.WallpapersScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan

enum class AppNavTab(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    EXPLORE("Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    PLAYLISTS("Playlists", Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic),
    LOCAL("Local", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    WALLPAPERS("Wallpapers", Icons.Filled.Wallpaper, Icons.Outlined.Wallpaper)
}

@Composable
fun MainScreen(
    repository: MusicRepository,
    playerManager: UnifiedPlayerManager
) {
    var selectedTab by remember { mutableStateOf(AppNavTab.HOME) }
    var activePlaylistId by remember { mutableStateOf<String?>(null) }
    var isFullPlayerExpanded by remember { mutableStateOf(false) }

    val currentTrack by playerManager.currentTrack.collectAsState()
    val favorites by repository.favorites.collectAsState()
    val isFavorite = currentTrack != null && favorites.contains(currentTrack!!.id)

    // Handle back button
    BackHandler(enabled = isFullPlayerExpanded || activePlaylistId != null || selectedTab != AppNavTab.HOME) {
        if (isFullPlayerExpanded) {
            isFullPlayerExpanded = false
            playerManager.setImmersiveWallpaperMode(false)
        } else if (activePlaylistId != null) {
            activePlaylistId = null
        } else if (selectedTab != AppNavTab.HOME) {
            selectedTab = AppNavTab.HOME
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Mini Player Bar docked above navigation bar
                    MiniPlayerBar(
                        playerManager = playerManager,
                        onExpand = { isFullPlayerExpanded = true }
                    )

                    // Material 3 Navigation Bar
                    NavigationBar(
                        containerColor = DarkSurface,
                        tonalElevation = 8.dp
                    ) {
                        AppNavTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab && activePlaylistId == null
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    activePlaylistId = null
                                    selectedTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (activePlaylistId != null) {
                    PlaylistDetailScreen(
                        playlistId = activePlaylistId!!,
                        repository = repository,
                        playerManager = playerManager,
                        onBack = { activePlaylistId = null }
                    )
                } else {
                    when (selectedTab) {
                        AppNavTab.HOME -> HomeScreen(
                            repository = repository,
                            playerManager = playerManager,
                            onNavigateToWallpapers = { selectedTab = AppNavTab.WALLPAPERS }
                        )
                        AppNavTab.EXPLORE -> ExploreScreen(
                            repository = repository,
                            playerManager = playerManager
                        )
                        AppNavTab.PLAYLISTS -> PlaylistsScreen(
                            repository = repository,
                            playerManager = playerManager,
                            onOpenPlaylist = { plId -> activePlaylistId = plId }
                        )
                        AppNavTab.LOCAL -> LocalMusicScreen(
                            repository = repository,
                            playerManager = playerManager
                        )
                        AppNavTab.WALLPAPERS -> WallpapersScreen(
                            playerManager = playerManager,
                            onExpandPlayer = { isFullPlayerExpanded = true }
                        )
                    }
                }
            }
        }

        // Full Screen Player Overlay
        AnimatedVisibility(
            visible = isFullPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullPlayerSheet(
                playerManager = playerManager,
                isFavorite = isFavorite,
                onToggleFavorite = {
                    currentTrack?.let { repository.toggleFavorite(it.id) }
                },
                onDismiss = {
                    isFullPlayerExpanded = false
                    playerManager.setImmersiveWallpaperMode(false)
                }
            )
        }
    }
}
