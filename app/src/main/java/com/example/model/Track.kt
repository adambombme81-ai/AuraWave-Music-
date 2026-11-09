package com.example.model

enum class MusicSource(val label: String, val badgeColorHex: Long) {
    YOUTUBE("YouTube", 0xFFFF0033),
    SPOTIFY("Spotify", 0xFF1ED760),
    LOCAL("Local Audio", 0xFF3A86FF),
    CURATED("Ambient Wave", 0xFF9D4EDD)
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val source: MusicSource,
    val sourceUri: String, // YouTube Video ID/URL, Spotify URI/Embed/Track ID, or Local content:// URI
    val artworkUrl: String? = null,
    val isFavorite: Boolean = false,
    val previewAudioUrl: String? = null, // Direct audio stream for instant playback
    val localUriString: String? = null
)
