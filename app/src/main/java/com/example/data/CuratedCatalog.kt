package com.example.data

import com.example.model.MusicSource
import com.example.model.Track

object CuratedCatalog {

    val youtubeTracks = listOf(
        Track(
            id = "yt_1",
            title = "Lofi Hip Hop Radio - Beats to Relax/Study to",
            artist = "Lofi Girl",
            album = "Peaceful Chills",
            durationMs = 18000000L, // Live stream/long mix
            source = MusicSource.YOUTUBE,
            sourceUri = "jfKfPfyJRdk",
            artworkUrl = "https://img.youtube.com/vi/jfKfPfyJRdk/hqdefault.jpg"
        ),
        Track(
            id = "yt_2",
            title = "Synthwave Radio - Chill Synth / Retro Beats",
            artist = "Lofi Girl Synth",
            album = "Neon Highways",
            durationMs = 12000000L,
            source = MusicSource.YOUTUBE,
            sourceUri = "4xDzrJKXOOY",
            artworkUrl = "https://img.youtube.com/vi/4xDzrJKXOOY/hqdefault.jpg"
        ),
        Track(
            id = "yt_3",
            title = "Cyberpunk 2077 Night City Ambient Mix",
            artist = "Cyber Audio Works",
            album = "Night City Radio",
            durationMs = 3600000L,
            source = MusicSource.YOUTUBE,
            sourceUri = "21X5lGlDOfg",
            artworkUrl = "https://img.youtube.com/vi/21X5lGlDOfg/hqdefault.jpg"
        ),
        Track(
            id = "yt_4",
            title = "Relaxing Piano Music - Beautiful Calm",
            artist = "Soothing Relaxation",
            album = "Tranquil Moments",
            durationMs = 10800000L,
            source = MusicSource.YOUTUBE,
            sourceUri = "1ZYbU82GVz4",
            artworkUrl = "https://img.youtube.com/vi/1ZYbU82GVz4/hqdefault.jpg"
        ),
        Track(
            id = "yt_5",
            title = "Chill Electronic Beats - Midnight Drift",
            artist = "ChilledCow Lab",
            album = "Midnight Drift",
            durationMs = 240000L,
            source = MusicSource.YOUTUBE,
            sourceUri = "7NOSDKb0HlU",
            artworkUrl = "https://img.youtube.com/vi/7NOSDKb0HlU/hqdefault.jpg"
        ),
        Track(
            id = "yt_6",
            title = "Deep Melodic House & Sunset Vibes",
            artist = "Sunset Soundscapes",
            album = "Golden Hour Beats",
            durationMs = 3800000L,
            source = MusicSource.YOUTUBE,
            sourceUri = "5qap5aO4i9A",
            artworkUrl = "https://img.youtube.com/vi/5qap5aO4i9A/hqdefault.jpg"
        )
    )

    val spotifyTracks = listOf(
        Track(
            id = "sp_1",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationMs = 200040L,
            source = MusicSource.SPOTIFY,
            sourceUri = "0VjIjW4GlUZAMYd2vXMi3b",
            artworkUrl = "https://i.scdn.co/image/ab67616d0000b2738863bc11d2aa12b54f5aeb36",
            previewAudioUrl = "https://p.scdn.co/mp3-preview/a8f2780bf59ca630b92e59e51cbb5b497f1f0a1d"
        ),
        Track(
            id = "sp_2",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            durationMs = 230453L,
            source = MusicSource.SPOTIFY,
            sourceUri = "7MXVkk9YM5IZxh0wAE26Vw",
            artworkUrl = "https://i.scdn.co/image/ab67616d0000b2734718e2b124f79258be7bc452"
        ),
        Track(
            id = "sp_3",
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationMs = 243266L,
            source = MusicSource.SPOTIFY,
            sourceUri = "6GyFP1nfCDB8Jyqi2E8rXr",
            artworkUrl = "https://i.scdn.co/image/ab67616d0000b27303c73491f074d284f67c30a2"
        ),
        Track(
            id = "sp_4",
            title = "Resonance",
            artist = "HOME",
            album = "Odyssey",
            durationMs = 212500L,
            source = MusicSource.SPOTIFY,
            sourceUri = "1TuopWDI4KhNdLfBrQIhhy",
            artworkUrl = "https://i.scdn.co/image/ab67616d0000b2733dc7ec320875e53ff9d8c9bc"
        ),
        Track(
            id = "sp_5",
            title = "Nightcall",
            artist = "Kavinsky",
            album = "OutRun",
            durationMs = 259000L,
            source = MusicSource.SPOTIFY,
            sourceUri = "0u695Uk7DsNVtDU4YvZYix",
            artworkUrl = "https://i.scdn.co/image/ab67616d0000b273010b904cbfe5d16f49ff31b3"
        ),
        Track(
            id = "sp_6",
            title = "Get Lucky",
            artist = "Daft Punk ft. Pharrell Williams",
            album = "Random Access Memories",
            durationMs = 248000L,
            source = MusicSource.SPOTIFY,
            sourceUri = "2Foc5Q5nqNiosCNqttzHof",
            artworkUrl = "https://i.scdn.co/image/ab67616d0000b2739b6ac98a52f64d50bbdbd134"
        )
    )

    val ambientTracks = listOf(
        Track(
            id = "amb_1",
            title = "Dreamy Synth Horizon",
            artist = "AuraWave Labs",
            album = "Cosmic Dreams",
            durationMs = 195000L,
            source = MusicSource.CURATED,
            sourceUri = "https://actions.google.com/sounds/v1/ambient/rain_heavy.ogg",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80",
            previewAudioUrl = "https://actions.google.com/sounds/v1/ambient/rain_heavy.ogg"
        ),
        Track(
            id = "amb_2",
            title = "Tokyo Midnight Rain",
            artist = "AuraWave Labs",
            album = "Neon Rain",
            durationMs = 220000L,
            source = MusicSource.CURATED,
            sourceUri = "https://actions.google.com/sounds/v1/weather/thunderstorm.ogg",
            artworkUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&q=80",
            previewAudioUrl = "https://actions.google.com/sounds/v1/weather/thunderstorm.ogg"
        ),
        Track(
            id = "amb_3",
            title = "Deep Space Pulse",
            artist = "AuraWave Labs",
            album = "Interstellar",
            durationMs = 180000L,
            source = MusicSource.CURATED,
            sourceUri = "https://actions.google.com/sounds/v1/science_fiction/space_engine_idle.ogg",
            artworkUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&q=80",
            previewAudioUrl = "https://actions.google.com/sounds/v1/science_fiction/space_engine_idle.ogg"
        ),
        Track(
            id = "amb_4",
            title = "Cyberpunk Metro Echo",
            artist = "AuraWave Labs",
            album = "Urban Resonance",
            durationMs = 210000L,
            source = MusicSource.CURATED,
            sourceUri = "https://actions.google.com/sounds/v1/transportation/subway_idle.ogg",
            artworkUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500&q=80",
            previewAudioUrl = "https://actions.google.com/sounds/v1/transportation/subway_idle.ogg"
        )
    )

    fun getAllInitialTracks(): List<Track> {
        return youtubeTracks + spotifyTracks + ambientTracks
    }
}
