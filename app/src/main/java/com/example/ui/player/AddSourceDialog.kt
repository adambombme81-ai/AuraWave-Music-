package com.example.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MusicSource
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.YouTubeRed

@Composable
fun AddSourceDialog(
    onDismiss: () -> Unit,
    onAddTrack: (title: String, artist: String, source: MusicSource, uri: String) -> Unit
) {
    var selectedSource by remember { mutableStateOf(MusicSource.YOUTUBE) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var inputUri by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Track",
                    tint = NeonCyan
                )
                Text(
                    text = "Add Custom Track",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SELECT SOURCE",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sources = listOf(MusicSource.YOUTUBE, MusicSource.SPOTIFY, MusicSource.CURATED)
                    sources.forEach { src ->
                        val isSel = src == selectedSource
                        val color = when (src) {
                            MusicSource.YOUTUBE -> YouTubeRed
                            MusicSource.SPOTIFY -> SpotifyGreen
                            else -> NeonCyan
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) color.copy(alpha = 0.25f) else DarkSurfaceElevated)
                                .clickable { selectedSource = src }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = src.label.substringBefore(" "),
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) color else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Track Title") },
                    placeholder = { Text("e.g. Midnight Beats") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_track_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist Name (Optional)") },
                    placeholder = { Text("e.g. Neon Wave") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_track_artist_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                val uriLabel = when (selectedSource) {
                    MusicSource.YOUTUBE -> "YouTube Video URL or ID"
                    MusicSource.SPOTIFY -> "Spotify Track URL or URI"
                    else -> "Audio Stream URL (.mp3 / .ogg)"
                }
                val uriPlaceholder = when (selectedSource) {
                    MusicSource.YOUTUBE -> "https://youtu.be/... or video ID"
                    MusicSource.SPOTIFY -> "https://open.spotify.com/track/..."
                    else -> "https://example.com/audio.mp3"
                }

                OutlinedTextField(
                    value = inputUri,
                    onValueChange = {
                        inputUri = it
                        errorMessage = null
                    },
                    label = { Text(uriLabel) },
                    placeholder = { Text(uriPlaceholder) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_track_uri_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputUri.isBlank()) {
                        errorMessage = "Please enter a valid URL or ID"
                        return@Button
                    }
                    val finalTitle = title.ifBlank {
                        if (selectedSource == MusicSource.YOUTUBE) "YouTube Track" else "Custom Track"
                    }
                    onAddTrack(finalTitle, artist, selectedSource, inputUri)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("confirm_add_track_button")
            ) {
                Text("Add Track", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}
