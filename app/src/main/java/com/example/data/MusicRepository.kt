package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistTrackCrossRef
import com.example.data.local.TrackEntity
import com.example.model.MusicSource
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList()
)

class MusicRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db = AppDatabase.getInstance(context)
    private val dao = db.musicDao()

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // Map of playlistId -> List of track IDs for fast reactive updates
    private val _playlistTrackMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())

    val playlists: StateFlow<List<Playlist>> = combine(
        dao.getAllPlaylists(),
        _playlistTrackMap,
        _allTracks
    ) { entities, map, tracks ->
        entities.map { entity ->
            val trackIds = map[entity.id] ?: emptyList()
            val plTracks = trackIds.mapNotNull { tid -> tracks.find { it.id == tid } }
            Playlist(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                trackCount = trackIds.size,
                tracks = plTracks
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        scope.launch {
            val initial = CuratedCatalog.getAllInitialTracks()
            _allTracks.value = initial
            dao.insertTracks(initial.map { TrackEntity.fromTrack(it) })

            // Check if initial playlists exist in Room
            val initialPlaylists = listOf(
                PlaylistEntity(
                    id = "pl_synth",
                    name = "Cyber Horizon",
                    description = "Futuristic synthwave & night city pulse"
                ),
                PlaylistEntity(
                    id = "pl_lofi",
                    name = "Late Night Study",
                    description = "Cozy chill beats and calm rain melodies"
                ),
                PlaylistEntity(
                    id = "pl_pop",
                    name = "Global Grooves",
                    description = "Chart-toppers from Spotify & YouTube"
                )
            )

            val initialMappings = mapOf(
                "pl_synth" to listOf("yt_2", "yt_3", "sp_3", "sp_5", "amb_1"),
                "pl_lofi" to listOf("yt_1", "yt_4", "amb_2"),
                "pl_pop" to listOf("sp_1", "sp_2", "sp_6", "yt_6")
            )

            initialPlaylists.forEach { pl ->
                dao.insertPlaylist(pl)
            }

            initialMappings.forEach { (plId, tids) ->
                tids.forEachIndexed { idx, tid ->
                    dao.addTrackToPlaylist(
                        PlaylistTrackCrossRef(playlistId = plId, trackId = tid, position = idx)
                    )
                }
            }

            refreshPlaylistTrackMap()

            _favorites.value = setOf("yt_1", "sp_1", "amb_1")
            dao.setTrackFavorite("yt_1", true)
            dao.setTrackFavorite("sp_1", true)
            dao.setTrackFavorite("amb_1", true)
        }
    }

    private suspend fun refreshPlaylistTrackMap() {
        val newMap = withContext(Dispatchers.IO) {
            val entities = listOf("pl_synth", "pl_lofi", "pl_pop")
            val map = mutableMapOf<String, List<String>>()
            entities.forEach { id ->
                map[id] = dao.getTrackIdsForPlaylistSync(id)
            }
            map
        }
        _playlistTrackMap.value = newMap
    }

    fun loadLocalTracks() {
        scope.launch {
            val deviceTracks = LocalMediaHelper.queryDeviceAudioFiles(context)
            if (deviceTracks.isNotEmpty()) {
                dao.insertTracks(deviceTracks.map { TrackEntity.fromTrack(it) })
                _allTracks.update { existing ->
                    val nonLocal = existing.filter { it.source != MusicSource.LOCAL }
                    nonLocal + deviceTracks
                }
            }
        }
    }

    fun addImportedTrack(track: Track) {
        scope.launch {
            dao.insertTrack(TrackEntity.fromTrack(track))
            _allTracks.update { list ->
                listOf(track) + list.filter { it.id != track.id }
            }
        }
    }

    fun addCustomTrack(
        title: String,
        artist: String,
        source: MusicSource,
        sourceUri: String
    ): Track {
        val cleanUri = parseSourceUri(source, sourceUri)
        val artwork = when (source) {
            MusicSource.YOUTUBE -> "https://img.youtube.com/vi/$cleanUri/hqdefault.jpg"
            MusicSource.SPOTIFY -> "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=500&q=80"
            MusicSource.LOCAL -> null
            MusicSource.CURATED -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80"
        }

        val newTrack = Track(
            id = "custom_${System.currentTimeMillis()}",
            title = title.ifBlank { "Custom Track" },
            artist = artist.ifBlank { "Unknown Artist" },
            album = "Added Tracks",
            durationMs = 180000L,
            source = source,
            sourceUri = cleanUri,
            artworkUrl = artwork,
            previewAudioUrl = if (cleanUri.startsWith("http") && !cleanUri.contains("youtube") && !cleanUri.contains("spotify")) cleanUri else null
        )

        scope.launch {
            dao.insertTrack(TrackEntity.fromTrack(newTrack))
            _allTracks.update { listOf(newTrack) + it }
        }

        return newTrack
    }

    private fun parseSourceUri(source: MusicSource, rawInput: String): String {
        val trimmed = rawInput.trim()
        return when (source) {
            MusicSource.YOUTUBE -> {
                when {
                    trimmed.contains("youtu.be/") -> trimmed.substringAfter("youtu.be/").substringBefore("?")
                    trimmed.contains("watch?v=") -> trimmed.substringAfter("watch?v=").substringBefore("&")
                    trimmed.contains("/embed/") -> trimmed.substringAfter("/embed/").substringBefore("?")
                    else -> trimmed
                }
            }
            MusicSource.SPOTIFY -> {
                when {
                    trimmed.contains("spotify.com/track/") -> trimmed.substringAfter("spotify.com/track/").substringBefore("?")
                    trimmed.contains("spotify:track:") -> trimmed.substringAfter("spotify:track:")
                    else -> trimmed
                }
            }
            else -> trimmed
        }
    }

    fun toggleFavorite(trackId: String) {
        scope.launch {
            val isNowFav = !_favorites.value.contains(trackId)
            _favorites.update { set ->
                if (isNowFav) set + trackId else set - trackId
            }
            dao.setTrackFavorite(trackId, isNowFav)
        }
    }

    // Playlist CRUD
    fun createPlaylist(name: String, description: String = ""): String {
        val id = "pl_${System.currentTimeMillis()}"
        scope.launch {
            val entity = PlaylistEntity(
                id = id,
                name = name.ifBlank { "New Playlist" },
                description = description
            )
            dao.insertPlaylist(entity)
            _playlistTrackMap.update { it + (id to emptyList()) }
        }
        return id
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        scope.launch {
            dao.updatePlaylistName(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: String) {
        scope.launch {
            dao.deletePlaylistById(playlistId)
            dao.deleteTracksForPlaylist(playlistId)
            _playlistTrackMap.update { it - playlistId }
        }
    }

    fun addTrackToPlaylist(playlistId: String, track: Track) {
        scope.launch {
            // Ensure track is stored in room
            dao.insertTrack(TrackEntity.fromTrack(track))
            val currentTracks = _playlistTrackMap.value[playlistId] ?: dao.getTrackIdsForPlaylistSync(playlistId)
            if (!currentTracks.contains(track.id)) {
                dao.addTrackToPlaylist(
                    PlaylistTrackCrossRef(
                        playlistId = playlistId,
                        trackId = track.id,
                        position = currentTracks.size
                    )
                )
                _playlistTrackMap.update { it + (playlistId to (currentTracks + track.id)) }
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        scope.launch {
            dao.removeTrackFromPlaylist(playlistId, trackId)
            val currentTracks = _playlistTrackMap.value[playlistId] ?: emptyList()
            _playlistTrackMap.update { it + (playlistId to (currentTracks - trackId)) }
        }
    }

    fun getPlaylistTracks(playlistId: String): Flow<List<Track>> {
        return dao.getTracksForPlaylist(playlistId).map { entities ->
            entities.map { it.toTrack() }
        }
    }
}
