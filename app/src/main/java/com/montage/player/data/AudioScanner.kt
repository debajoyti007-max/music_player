package com.montage.player.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.montage.player.model.AudioTrack

object AudioScanner {

    fun scanStorageAudio(context: Context): List<AudioTrack> {
        val tracks = mutableListOf<AudioTrack>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Filter out ringtones or very short sounds under 5 seconds
                    if (duration > 5000) {
                        tracks.add(
                            AudioTrack(
                                id = id,
                                title = title,
                                artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                                album = if (album == "<unknown>") "Local Music" else album,
                                durationMs = duration,
                                contentUri = contentUri,
                                albumId = albumId,
                                path = path
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tracks
    }

    fun getSampleOfflineTracks(): List<AudioTrack> {
        return listOf(
            AudioTrack(
                id = 1001L,
                title = "Midnight Horizon",
                artist = "Luna Eclipse",
                album = "Nocturnal Echoes",
                durationMs = 214000L,
                contentUri = Uri.parse("https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3?filename=lofi-study-112191.mp3")
            ),
            AudioTrack(
                id = 1002L,
                title = "Solar Drift",
                artist = "Aether Collective",
                album = "Atmospheres Vol. 2",
                durationMs = 188000L,
                contentUri = Uri.parse("https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3?filename=electronic-future-beats-117997.mp3")
            ),
            AudioTrack(
                id = 1003L,
                title = "Velvet Rain",
                artist = "Kaelen Vance",
                album = "Reflections in Grey",
                durationMs = 245000L,
                contentUri = Uri.parse("https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3?filename=chill-abstract-intention-12099.mp3")
            ),
            AudioTrack(
                id = 1004L,
                title = "Urban Pulse",
                artist = "Nova Soundlab",
                album = "Metropolitan High",
                durationMs = 195000L,
                contentUri = Uri.parse("https://cdn.pixabay.com/download/audio/2021/09/06/audio_7301c27e85.mp3?filename=ambient-piano-amp-strings-10711.mp3")
            )
        )
    }
}
