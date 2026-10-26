package com.montage.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.montage.player.player.MusicPlaybackManager
import com.montage.player.theme.MontagePlayerTheme
import com.montage.player.theme.SpotifyBackground
import com.montage.player.ui.main.MainScreen

class MainActivity : ComponentActivity() {

    private lateinit var playbackManager: MusicPlaybackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackManager = MusicPlaybackManager(applicationContext)

        enableEdgeToEdge()
        setContent {
            MontagePlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SpotifyBackground
                ) {
                    MainScreen(playbackManager = playbackManager)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackManager.release()
    }
}
