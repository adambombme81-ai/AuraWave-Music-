package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.model.EqualizerPreset
import com.example.model.MusicSource
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.model.WallpaperPalette
import com.example.model.WallpaperType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class UnifiedPlayerManager(private val context: Context) {

    private val TAG = "UnifiedPlayerManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var mediaPlayer: MediaPlayer? = null
    private var progressTrackingJob: Job? = null
    private var visualizerJob: Job? = null
    private var sleepTimerJob: Job? = null

    // Callbacks for WebView players (YouTube, Spotify)
    var onYouTubeCommand: ((action: String, arg: String) -> Unit)? = null
    var onSpotifyCommand: ((action: String, arg: String) -> Unit)? = null

    // Playback state
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Wallpaper configuration
    private val _wallpaperType = MutableStateFlow(WallpaperType.SYNTHWAVE)
    val wallpaperType: StateFlow<WallpaperType> = _wallpaperType.asStateFlow()

    private val _wallpaperPalette = MutableStateFlow(WallpaperPalette.CYBER_NEON)
    val wallpaperPalette: StateFlow<WallpaperPalette> = _wallpaperPalette.asStateFlow()

    private val _wallpaperSensitivity = MutableStateFlow(1.0f)
    val wallpaperSensitivity: StateFlow<Float> = _wallpaperSensitivity.asStateFlow()

    private val _showVideoInsteadOfWallpaper = MutableStateFlow(false)
    val showVideoInsteadOfWallpaper: StateFlow<Boolean> = _showVideoInsteadOfWallpaper.asStateFlow()

    private val _isImmersiveWallpaperMode = MutableStateFlow(false)
    val isImmersiveWallpaperMode: StateFlow<Boolean> = _isImmersiveWallpaperMode.asStateFlow()

    // Audio Visualizer real-time reactive state
    private val _visualizerBands = MutableStateFlow(FloatArray(32) { 0.1f })
    val visualizerBands: StateFlow<FloatArray> = _visualizerBands.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0.1f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    // Equalizer & FX
    private val _equalizerPreset = MutableStateFlow(EqualizerPreset.BALANCED)
    val equalizerPreset: StateFlow<EqualizerPreset> = _equalizerPreset.asStateFlow()

    private val _bassLevel = MutableStateFlow(0.5f)
    val bassLevel: StateFlow<Float> = _bassLevel.asStateFlow()

    private val _midLevel = MutableStateFlow(0.5f)
    val midLevel: StateFlow<Float> = _midLevel.asStateFlow()

    private val _trebleLevel = MutableStateFlow(0.5f)
    val trebleLevel: StateFlow<Float> = _trebleLevel.asStateFlow()

    // Sleep Timer (remaining minutes or null)
    private val _sleepTimerRemainingMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingMinutes: StateFlow<Int?> = _sleepTimerRemainingMinutes.asStateFlow()

    init {
        startVisualizerLoop()
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (newQueue != null && newQueue.isNotEmpty()) {
            _queue.value = newQueue
            _queueIndex.value = newQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        } else if (!_queue.value.any { it.id == track.id }) {
            _queue.update { listOf(track) + it }
            _queueIndex.value = 0
        } else {
            _queueIndex.value = _queue.value.indexOfFirst { it.id == track.id }
        }

        _currentTrack.value = track
        _currentPositionMs.value = 0L
        _durationMs.value = if (track.durationMs > 0) track.durationMs else 180000L

        stopMediaPlayer()

        when (track.source) {
            MusicSource.LOCAL -> {
                playLocalOrUriTrack(track.localUriString ?: track.sourceUri)
            }
            MusicSource.CURATED -> {
                val stream = track.previewAudioUrl ?: track.sourceUri
                playLocalOrUriTrack(stream)
            }
            MusicSource.YOUTUBE -> {
                _isPlaying.value = true
                onYouTubeCommand?.invoke("load", track.sourceUri)
                startProgressTracker()
            }
            MusicSource.SPOTIFY -> {
                _isPlaying.value = true
                // If Spotify preview is available, we can also play preview via MediaPlayer
                if (!track.previewAudioUrl.isNullOrBlank()) {
                    playLocalOrUriTrack(track.previewAudioUrl)
                } else {
                    onSpotifyCommand?.invoke("load", track.sourceUri)
                    startProgressTracker()
                }
            }
        }
    }

    private fun playLocalOrUriTrack(uriString: String) {
        try {
            _isBuffering.value = true
            stopMediaPlayer()

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (uriString.startsWith("content://") || uriString.startsWith("android.resource://")) {
                    setDataSource(context, Uri.parse(uriString))
                } else {
                    setDataSource(uriString)
                }

                setOnPreparedListener { mp ->
                    _isBuffering.value = false
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(1000L)
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracker()
                }

                setOnCompletionListener {
                    onTrackCompleted()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    _isBuffering.value = false
                    _isPlaying.value = false
                    true
                }

                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaPlayer for uri: $uriString", e)
            _isBuffering.value = false
            // Fallback progress simulation
            _isPlaying.value = true
            startProgressTracker()
        }
    }

    fun togglePlayPause() {
        val playing = _isPlaying.value
        val track = _currentTrack.value ?: return

        if (playing) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _isPlaying.value = false
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
        val track = _currentTrack.value
        when (track?.source) {
            MusicSource.YOUTUBE -> onYouTubeCommand?.invoke("pause", "")
            MusicSource.SPOTIFY -> onSpotifyCommand?.invoke("pause", "")
            else -> {}
        }
    }

    fun resume() {
        _isPlaying.value = true
        mediaPlayer?.let {
            it.start()
            startProgressTracker()
        }
        val track = _currentTrack.value
        when (track?.source) {
            MusicSource.YOUTUBE -> {
                onYouTubeCommand?.invoke("play", "")
                startProgressTracker()
            }
            MusicSource.SPOTIFY -> {
                onSpotifyCommand?.invoke("play", "")
                startProgressTracker()
            }
            else -> {
                if (mediaPlayer == null && track != null) {
                    playTrack(track)
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1000L))
        _currentPositionMs.value = clamped

        mediaPlayer?.let {
            try {
                it.seekTo(clamped.toInt())
            } catch (e: Exception) {
                Log.w(TAG, "Seek error", e)
            }
        }

        val track = _currentTrack.value
        if (track?.source == MusicSource.YOUTUBE) {
            onYouTubeCommand?.invoke("seek", (clamped / 1000f).toString())
        }
    }

    fun next() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIndex = if (_isShuffle.value) {
            Random.nextInt(q.size)
        } else {
            (_queueIndex.value + 1) % q.size
        }
        _queueIndex.value = nextIndex
        playTrack(q[nextIndex])
    }

    fun previous() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (_queueIndex.value > 0) _queueIndex.value - 1 else q.size - 1
        _queueIndex.value = prevIndex
        playTrack(q[prevIndex])
    }

    fun toggleShuffle() {
        _isShuffle.update { !it }
    }

    fun toggleRepeat() {
        _repeatMode.update {
            when (it) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
        }
    }

    private fun onTrackCompleted() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0L)
                resume()
            }
            RepeatMode.ALL -> next()
            RepeatMode.OFF -> {
                if (_queueIndex.value < _queue.value.size - 1) {
                    next()
                } else {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                }
            }
        }
    }

    // Callbacks from YouTube / Spotify WebViews
    fun updateFromExternalPlayer(positionSec: Float, durationSec: Float, isPlayingExternal: Boolean) {
        if (_currentTrack.value?.source == MusicSource.YOUTUBE || _currentTrack.value?.source == MusicSource.SPOTIFY) {
            _currentPositionMs.value = (positionSec * 1000).toLong()
            if (durationSec > 0) {
                _durationMs.value = (durationSec * 1000).toLong()
            }
            _isPlaying.value = isPlayingExternal
        }
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let {
                    try {
                        if (it.isPlaying) {
                            _currentPositionMs.value = it.currentPosition.toLong()
                        }
                    } catch (e: Exception) {
                        // ignore during transient states
                    }
                } ?: run {
                    // For external or simulated streams, advance timeline
                    val dur = _durationMs.value
                    if (dur > 0) {
                        val newPos = _currentPositionMs.value + 500L
                        if (newPos >= dur) {
                            onTrackCompleted()
                        } else {
                            _currentPositionMs.value = newPos
                        }
                    }
                }
                delay(500L)
            }
        }
    }

    private fun stopMediaPlayer() {
        progressTrackingJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
    }

    // Live Wallpaper & Audio Visualizer simulation
    private fun startVisualizerLoop() {
        visualizerJob?.cancel()
        visualizerJob = scope.launch(Dispatchers.Default) {
            var tick = 0.0
            while (isActive) {
                val playing = _isPlaying.value
                val sensitivity = _wallpaperSensitivity.value
                val bass = _bassLevel.value
                val treble = _trebleLevel.value

                tick += if (playing) 0.15 else 0.02

                val bands = FloatArray(32) { i ->
                    if (playing) {
                        val baseWave = (sin(tick * 1.5 + i * 0.4) + 1.0) / 2.0
                        val harmonic = (sin(tick * 3.2 - i * 0.8) + 1.0) / 4.0
                        val jitter = Random.nextFloat() * 0.25f
                        val eqFactor = if (i < 8) (0.5f + bass * 0.8f) else if (i > 20) (0.5f + treble * 0.8f) else 1.0f

                        ((baseWave + harmonic + jitter) * sensitivity * eqFactor)
                            .toFloat()
                            .coerceIn(0.05f, 1.0f)
                    } else {
                        // Idle gentle breathing wave
                        (0.08f + sin(tick * 0.5 + i * 0.2).toFloat() * 0.04f).coerceAtLeast(0.02f)
                    }
                }

                _visualizerBands.value = bands
                _currentAmplitude.value = bands.average().toFloat()

                delay(40L) // ~25-30 fps audio visualization updates
            }
        }
    }

    // Wallpaper controls
    fun setWallpaperType(type: WallpaperType) {
        _wallpaperType.value = type
    }

    fun setWallpaperPalette(palette: WallpaperPalette) {
        _wallpaperPalette.value = palette
    }

    fun setWallpaperSensitivity(sensitivity: Float) {
        _wallpaperSensitivity.value = sensitivity.coerceIn(0.2f, 2.5f)
    }

    fun toggleShowVideo() {
        _showVideoInsteadOfWallpaper.update { !it }
    }

    fun toggleImmersiveWallpaperMode() {
        _isImmersiveWallpaperMode.update { !it }
    }

    fun setImmersiveWallpaperMode(immersive: Boolean) {
        _isImmersiveWallpaperMode.value = immersive
    }

    // Equalizer controls
    fun applyEqualizerPreset(preset: EqualizerPreset) {
        _equalizerPreset.value = preset
        _bassLevel.value = preset.bass
        _midLevel.value = preset.mid
        _trebleLevel.value = preset.treble
    }

    fun setBass(level: Float) {
        _bassLevel.value = level.coerceIn(0f, 1f)
    }

    fun setMid(level: Float) {
        _midLevel.value = level.coerceIn(0f, 1f)
    }

    fun setTreble(level: Float) {
        _trebleLevel.value = level.coerceIn(0f, 1f)
    }

    // Sleep Timer
    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingMinutes.value = minutes

        if (minutes != null && minutes > 0) {
            sleepTimerJob = scope.launch {
                var remaining = minutes
                while (remaining > 0 && isActive) {
                    delay(60000L) // 1 minute
                    remaining--
                    _sleepTimerRemainingMinutes.value = remaining
                }
                if (remaining == 0) {
                    pause()
                    _sleepTimerRemainingMinutes.value = null
                }
            }
        }
    }

    fun release() {
        stopMediaPlayer()
        visualizerJob?.cancel()
        sleepTimerJob?.cancel()
    }
}
