package com.example.model

import androidx.compose.ui.graphics.Color

enum class WallpaperType(
    val title: String,
    val subtitle: String
) {
    SYNTHWAVE("Retro Synthwave", "Neon sun, 3D perspective grid & pulsing horizon"),
    CYBERPUNK("Equalizer Matrix", "Reactive multi-band spectrum bars with glowing peaks"),
    NEBULA("Cosmic Nebula", "Hyperspace warp stars with swirling interstellar auroras"),
    LOFI_RAIN("Lofi Rain & Vinyl", "Animated rain condensation on glass with spinning vinyl"),
    LIQUID_LAVA("Liquid Flow", "Morphing audio-reactive fluid gradient orbs")
}

enum class WallpaperPalette(
    val title: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color
) {
    CYBER_NEON("Cyber Neon", Color(0xFF00F0FF), Color(0xFFFF2E93), Color(0xFF9D4EDD)),
    SUNSET_HORIZON("Sunset Horizon", Color(0xFFFF8A00), Color(0xFFFF007A), Color(0xFF7928CA)),
    EMERALD_AURORA("Emerald Aurora", Color(0xFF1ED760), Color(0xFF00D2FF), Color(0xFF0061FF)),
    VIOLET_DREAM("Violet Mirage", Color(0xFFB5179E), Color(0xFF7209B7), Color(0xFF4361EE)),
    SOLAR_INFERNO("Solar Flare", Color(0xFFFF416C), Color(0xFFFF4B2B), Color(0xFFFF8C00))
}

enum class EqualizerPreset(
    val title: String,
    val bass: Float,
    val mid: Float,
    val treble: Float
) {
    BALANCED("Balanced Flat", 0.5f, 0.5f, 0.5f),
    BASS_BOOST("Bass Boost", 0.95f, 0.45f, 0.40f),
    ELECTRONIC("Electronic", 0.85f, 0.60f, 0.85f),
    VOCAL_FOCUS("Vocal Clarify", 0.35f, 0.90f, 0.65f),
    ACOUSTIC("Acoustic Warm", 0.60f, 0.75f, 0.70f),
    LOFI_CHILL("Lo-Fi Soft", 0.75f, 0.50f, 0.30f)
}
