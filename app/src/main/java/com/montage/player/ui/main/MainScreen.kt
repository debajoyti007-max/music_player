package com.montage.player.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.montage.player.R
import com.montage.player.data.AudioScanner
import com.montage.player.model.AudioTrack
import com.montage.player.player.MusicPlaybackManager
import com.montage.player.player.RepeatMode
import com.montage.player.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    playbackManager: MusicPlaybackManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // State flows from Playback Manager
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val progressMs by playbackManager.progressMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val isShuffle by playbackManager.isShuffle.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()

    var tracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var hasPermission by remember { mutableStateOf(false) }
    var isExpandedNowPlaying by remember { mutableStateOf(false) }

    // Required permission based on Android SDK level
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            val scanned = AudioScanner.scanStorageAudio(context)
            tracks = if (scanned.isNotEmpty()) scanned else AudioScanner.getSampleOfflineTracks()
        } else {
            tracks = AudioScanner.getSampleOfflineTracks()
        }
    }

    // Initial check on launch
    LaunchedEffect(Unit) {
        val check = ContextCompat.checkSelfPermission(context, requiredPermission)
        if (check == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
            val scanned = AudioScanner.scanStorageAudio(context)
            tracks = if (scanned.isNotEmpty()) scanned else AudioScanner.getSampleOfflineTracks()
        } else {
            permissionLauncher.launch(requiredPermission)
        }
    }

    val filteredTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) tracks
        else tracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = SpotifyBackground,
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (currentTrack != null) {
                MiniPlayerBar(
                    track = currentTrack!!,
                    isPlaying = isPlaying,
                    progressMs = progressMs,
                    durationMs = durationMs,
                    onTogglePlay = { playbackManager.togglePlayPause() },
                    onNext = { playbackManager.next() },
                    onClick = { isExpandedNowPlaying = true }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Dynamic Ambient Header Glow Wash
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SpotifyGreen.copy(alpha = 0.18f),
                                SpotifyBackground
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // ── Top Header with Bespoke Montage Logo & Scan Button ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_montage_logo),
                            contentDescription = "Montage Emblem",
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MONTAGE",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SpotifyGreen)
                                )
                            }
                            Text(
                                text = "STUDIO • OFFLINE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = SpotifyGreen,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Rescan Storage Button
                    IconButton(
                        onClick = {
                            val scanned = AudioScanner.scanStorageAudio(context)
                            tracks = if (scanned.isNotEmpty()) scanned else AudioScanner.getSampleOfflineTracks()
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SpotifyElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Scan Phone Storage",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Search Field ──
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search your local music...", color = SpotifyTextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = SpotifyTextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = SpotifyTextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SpotifySurface,
                        unfocusedContainerColor = SpotifySurface,
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Permission Callout Banner (if permission not granted)
                if (!hasPermission) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpotifyElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grant storage permission to load all phone audio",
                                fontSize = 11.sp,
                                color = SpotifyTextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { permissionLauncher.launch(requiredPermission) },
                                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Grant", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Track Count Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredTracks.size} Tracks Found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyTextSecondary
                    )
                    Text(
                        text = "Phone Storage",
                        fontSize = 11.sp,
                        color = SpotifyTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Tracks List ──
                if (filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "No audio tracks found.\nTap refresh above to scan storage.",
                            color = SpotifyTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(filteredTracks) { index, track ->
                            val isCurrent = currentTrack?.id == track.id
                            TrackListItem(
                                index = index + 1,
                                track = track,
                                isCurrent = isCurrent,
                                isPlaying = isCurrent && isPlaying,
                                onClick = {
                                    playbackManager.playTrack(track, filteredTracks)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Fullscreen Expanded Now Playing Bottom Sheet ──
    if (isExpandedNowPlaying && currentTrack != null) {
        ModalBottomSheet(
            onDismissRequest = { isExpandedNowPlaying = false },
            containerColor = SpotifySurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = SpotifyTextMuted) },
            modifier = Modifier.fillMaxHeight(0.92f)
        ) {
            ExpandedNowPlayingContent(
                track = currentTrack!!,
                isPlaying = isPlaying,
                progressMs = progressMs,
                durationMs = durationMs,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                onTogglePlay = { playbackManager.togglePlayPause() },
                onNext = { playbackManager.next() },
                onPrevious = { playbackManager.previous() },
                onSeek = { playbackManager.seekTo(it) },
                onToggleShuffle = { playbackManager.toggleShuffle() },
                onCycleRepeat = { playbackManager.cycleRepeat() }
            )
        }
    }
}

// ── Dancing Audio Equalizer Bar Animation ──
@Composable
fun SpotifyDancingEqualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(750, easing = LinearEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "b1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "b2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "b3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(550, easing = LinearEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "b4"
    )

    Row(
        modifier = Modifier.height(18.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val activeH1 = if (isPlaying) h1.dp else 4.dp
        val activeH2 = if (isPlaying) h2.dp else 10.dp
        val activeH3 = if (isPlaying) h3.dp else 6.dp
        val activeH4 = if (isPlaying) h4.dp else 12.dp

        Box(modifier = Modifier.width(3.dp).height(activeH1).clip(RoundedCornerShape(1.dp)).background(SpotifyGreen))
        Box(modifier = Modifier.width(3.dp).height(activeH2).clip(RoundedCornerShape(1.dp)).background(SpotifyGreen))
        Box(modifier = Modifier.width(3.dp).height(activeH3).clip(RoundedCornerShape(1.dp)).background(SpotifyGreen))
        Box(modifier = Modifier.width(3.dp).height(activeH4).clip(RoundedCornerShape(1.dp)).background(SpotifyGreen))
    }
}

// ── Track Row Item ──
@Composable
fun TrackListItem(
    index: Int,
    track: AudioTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Index or Animated Spotify Equalizer
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent) {
                SpotifyDancingEqualizer(isPlaying = isPlaying)
            } else {
                Text(
                    text = "$index",
                    fontSize = 12.sp,
                    color = SpotifyTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrent) SpotifyGreen else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                fontSize = 12.sp,
                color = SpotifyTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(
            text = track.formattedDuration(),
            fontSize = 12.sp,
            color = SpotifyTextSecondary
        )
    }
}

// ── Persistent Mini Player Bar ──
@Composable
fun MiniPlayerBar(
    track: AudioTrack,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit
) {
    val progress = if (durationMs > 0) (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    Surface(
        color = SpotifyElevated,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Live Mini Scrubber Indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = SpotifyGreen,
                trackColor = SpotifyHighlight
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Vinyl / Artwork Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SpotifyHighlight, SpotifySurface)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        fontSize = 11.sp,
                        color = SpotifyTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controls
                IconButton(onClick = onTogglePlay) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

// ── Fullscreen Expanded Player Sheet ──
@Composable
fun ExpandedNowPlayingContent(
    track: AudioTrack,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Vinyl Album Art
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(SpotifyHighlight, Color.Black)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Album,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(140.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title and Artist
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artist,
                    fontSize = 14.sp,
                    color = SpotifyTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = "Favorite",
                tint = SpotifyGreen,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seek Slider
        val currentSliderValue = progressMs.toFloat().coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f))
        Slider(
            value = currentSliderValue,
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = SpotifyGreen,
                inactiveTrackColor = SpotifyHighlight
            )
        )

        // Elapsed / Total Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val elapsedSec = progressMs / 1000
            val totalSec = durationMs / 1000
            Text(
                text = "%d:%02d".format(elapsedSec / 60, elapsedSec % 60),
                fontSize = 11.sp,
                color = SpotifyTextSecondary
            )
            Text(
                text = "%d:%02d".format(totalSec / 60, totalSec % 60),
                fontSize = 11.sp,
                color = SpotifyTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) SpotifyGreen else SpotifyTextSecondary
                )
            }

            // Previous
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Circular Play/Pause
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onTogglePlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat
            IconButton(onClick = onCycleRepeat) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.ONE -> Icons.Rounded.RepeatOne
                        RepeatMode.ALL -> Icons.Rounded.Repeat
                        RepeatMode.OFF -> Icons.Rounded.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.OFF) SpotifyGreen else SpotifyTextSecondary
                )
            }
        }
    }
}
