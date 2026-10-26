package com.montage.player.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.montage.player.model.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

class MusicPlaybackManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progressMs = MutableStateFlow(0L)
    val progressMs: StateFlow<Long> = _progressMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<AudioTrack>>(emptyList())
    val queue: StateFlow<List<AudioTrack>> = _queue.asStateFlow()

    private var currentIndex = 0

    init {
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                handleTrackCompletion()
            }
            setOnErrorListener { _, _, _ ->
                _isPlaying.value = false
                false
            }
        }
    }

    fun playTrack(track: AudioTrack, newQueue: List<AudioTrack>? = null) {
        if (newQueue != null) {
            _queue.value = newQueue
            currentIndex = newQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        } else {
            val q = _queue.value
            val idx = q.indexOfFirst { it.id == track.id }
            if (idx >= 0) {
                currentIndex = idx
            } else {
                _queue.value = listOf(track) + q
                currentIndex = 0
            }
        }

        _currentTrack.value = track
        _durationMs.value = track.durationMs
        _progressMs.value = 0L

        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(context, track.contentUri)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener { mp ->
                mp.start()
                _isPlaying.value = true
                _durationMs.value = mp.duration.toLong()
                startProgressTracker()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
        } else {
            if (_currentTrack.value != null) {
                player.start()
                _isPlaying.value = true
                startProgressTracker()
            } else if (_queue.value.isNotEmpty()) {
                playTrack(_queue.value.first())
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
        _progressMs.value = positionMs
    }

    fun next() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_repeatMode.value == RepeatMode.ONE && _currentTrack.value != null) {
            seekTo(0)
            mediaPlayer?.start()
            _isPlaying.value = true
            return
        }

        var nextIdx = currentIndex + 1
        if (_isShuffle.value) {
            nextIdx = (q.indices).random()
        }

        if (nextIdx >= q.size) {
            if (_repeatMode.value == RepeatMode.ALL) {
                nextIdx = 0
            } else {
                _isPlaying.value = false
                mediaPlayer?.pause()
                return
            }
        }

        currentIndex = nextIdx
        playTrack(q[nextIdx])
    }

    fun previous() {
        if (_progressMs.value > 3000) {
            seekTo(0)
            return
        }

        val q = _queue.value
        if (q.isEmpty()) return

        var prevIdx = currentIndex - 1
        if (prevIdx < 0) {
            prevIdx = q.size - 1
        }

        currentIndex = prevIdx
        playTrack(q[prevIdx])
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun cycleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    private fun handleTrackCompletion() {
        next()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _progressMs.value = it.currentPosition.toLong()
                    }
                }
                delay(300)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    fun release() {
        stopProgressTracker()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
