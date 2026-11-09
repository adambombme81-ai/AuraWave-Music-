package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.MusicSource
import com.example.model.Track

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val source: String,
    val sourceUri: String,
    val artworkUrl: String? = null,
    val previewAudioUrl: String? = null,
    val localUriString: String? = null,
    val isFavorite: Boolean = false
) {
    fun toTrack(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            source = try { MusicSource.valueOf(source) } catch (e: Exception) { MusicSource.CURATED },
            sourceUri = sourceUri,
            artworkUrl = artworkUrl,
            previewAudioUrl = previewAudioUrl,
            localUriString = localUriString,
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromTrack(track: Track): TrackEntity {
            return TrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMs = track.durationMs,
                source = track.source.name,
                sourceUri = track.sourceUri,
                artworkUrl = track.artworkUrl,
                previewAudioUrl = track.previewAudioUrl,
                localUriString = track.localUriString,
                isFavorite = track.isFavorite
            )
        }
    }
}

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    indices = [Index("playlistId"), Index("trackId")]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val position: Int = 0
)
