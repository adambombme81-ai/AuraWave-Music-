package com.example

import com.example.data.CuratedCatalog
import com.example.data.Playlist
import com.example.model.EqualizerPreset
import com.example.model.MusicSource
import com.example.model.Track
import com.example.model.WallpaperPalette
import com.example.model.WallpaperType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test curated catalog has required multi-source tracks`() {
        val tracks = CuratedCatalog.getAllInitialTracks()
        assertTrue("Catalog should contain tracks", tracks.isNotEmpty())

        val ytTracks = tracks.filter { it.source == MusicSource.YOUTUBE }
        val spTracks = tracks.filter { it.source == MusicSource.SPOTIFY }
        val ambientTracks = tracks.filter { it.source == MusicSource.CURATED }

        assertTrue("Should have YouTube tracks", ytTracks.isNotEmpty())
        assertTrue("Should have Spotify tracks", spTracks.isNotEmpty())
        assertTrue("Should have Ambient / Curated tracks", ambientTracks.isNotEmpty())

        // Check YouTube IDs
        ytTracks.forEach {
            assertTrue("YouTube track should have valid URI or ID", it.sourceUri.isNotBlank())
            assertNotNull("YouTube track should have thumbnail artwork", it.artworkUrl)
        }

        // Check Spotify tracks
        spTracks.forEach {
            assertTrue("Spotify track should have valid URI or ID", it.sourceUri.isNotBlank())
        }
    }

    @Test
    fun `test playlist creation and management logic`() {
        val initialTracks = listOf(
            Track(
                id = "t1",
                title = "Cyber Beat",
                artist = "Neon",
                source = MusicSource.YOUTUBE,
                sourceUri = "xyz123"
            ),
            Track(
                id = "t2",
                title = "Local Sunset",
                artist = "Audio Artist",
                source = MusicSource.LOCAL,
                sourceUri = "content://media/123"
            )
        )

        val playlist = Playlist(
            id = "pl_test",
            name = "My Test Playlist",
            description = "Test description",
            trackCount = initialTracks.size,
            tracks = initialTracks
        )

        assertEquals("pl_test", playlist.id)
        assertEquals("My Test Playlist", playlist.name)
        assertEquals(2, playlist.trackCount)
        assertEquals(2, playlist.tracks.size)

        // Test renaming
        val renamed = playlist.copy(name = "Updated Playlist Name")
        assertEquals("Updated Playlist Name", renamed.name)

        // Test adding a Spotify song
        val spSong = Track(
            id = "t3",
            title = "Blinding Lights",
            artist = "The Weeknd",
            source = MusicSource.SPOTIFY,
            sourceUri = "0VjIjW4GlUZAMYd2vXMi3b"
        )
        val withAdded = renamed.copy(
            tracks = renamed.tracks + spSong,
            trackCount = renamed.tracks.size + 1
        )
        assertEquals(3, withAdded.trackCount)
        assertTrue(withAdded.tracks.any { it.source == MusicSource.SPOTIFY })

        // Test removing a song
        val withRemoved = withAdded.copy(
            tracks = withAdded.tracks.filter { it.id != "t1" },
            trackCount = withAdded.tracks.size - 1
        )
        assertEquals(2, withRemoved.trackCount)
        assertFalse(withRemoved.tracks.any { it.id == "t1" })
    }

    @Test
    fun `test live wallpaper themes and presets`() {
        val wallpapers = WallpaperType.entries
        assertEquals(5, wallpapers.size)
        assertTrue(wallpapers.contains(WallpaperType.SYNTHWAVE))
        assertTrue(wallpapers.contains(WallpaperType.CYBERPUNK))
        assertTrue(wallpapers.contains(WallpaperType.NEBULA))
        assertTrue(wallpapers.contains(WallpaperType.LOFI_RAIN))
        assertTrue(wallpapers.contains(WallpaperType.LIQUID_LAVA))

        val palettes = WallpaperPalette.entries
        assertTrue(palettes.isNotEmpty())

        val eqPresets = EqualizerPreset.entries
        assertTrue(eqPresets.isNotEmpty())
        assertTrue(eqPresets.contains(EqualizerPreset.BASS_BOOST))
    }
}
