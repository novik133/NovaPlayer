package com.novaplayer.app.playback

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.novaplayer.app.MainActivity
import com.novaplayer.app.NovaPlayerApplication
import com.novaplayer.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var crossfadeJob: Job? = null
    private var crossfadeDurationMs: Int = 0

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build()
        currentAudioSessionId = player.audioSessionId

        mediaSession = MediaSession.Builder(this, player).build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (crossfadeDurationMs > 0) {
                    player.volume = 0f
                    fadeIn(player)
                } else {
                    player.volume = 1f
                }
            }
        })

        // Observe settings changes
        startSettingsObserver(player)

        startForeground(
            NovaPlayerApplication.PLAYBACK_NOTIFICATION_ID,
            createNotification()
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        crossfadeJob?.cancel()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        currentAudioSessionId = 0
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        return NotificationCompat.Builder(this, NovaPlayerApplication.PLAYBACK_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.now_playing))
            .setSmallIcon(R.drawable.ic_nova_player)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun startCrossfadeMonitor() {
        crossfadeJob?.cancel()
        if (crossfadeDurationMs <= 0) return

        crossfadeJob = serviceScope.launch {
            while (isActive) {
                val player = (mediaSession?.player as? ExoPlayer) ?: break
                if (player.isPlaying && player.duration > 0) {
                    val remaining = player.duration - player.currentPosition
                    if (remaining in 1..crossfadeDurationMs.toLong()) {
                        val fraction = remaining.toFloat() / crossfadeDurationMs
                        player.volume = fraction.coerceIn(0.05f, 1f)
                    }
                }
                delay(50)
            }
        }
    }

    private fun fadeIn(player: ExoPlayer) {
        serviceScope.launch {
            val steps = 20
            val stepDuration = (crossfadeDurationMs / steps).toLong().coerceAtLeast(30)
            for (i in 1..steps) {
                if (!isActive) break
                player.volume = (i.toFloat() / steps).coerceIn(0f, 1f)
                delay(stepDuration)
            }
            player.volume = 1f
        }
    }

    private fun startSettingsObserver(player: ExoPlayer) {
        serviceScope.launch {
            var lastSkipSilence = skipSilenceEnabled
            var lastCrossfade = pendingCrossfadeDurationMs
            while (isActive) {
                if (skipSilenceEnabled != lastSkipSilence) {
                    lastSkipSilence = skipSilenceEnabled
                    player.skipSilenceEnabled = lastSkipSilence
                }
                if (pendingCrossfadeDurationMs != lastCrossfade) {
                    lastCrossfade = pendingCrossfadeDurationMs
                    crossfadeDurationMs = lastCrossfade
                    startCrossfadeMonitor()
                }
                delay(200)
            }
        }
    }

    fun setQueue(uris: List<String>, startIndex: Int) {
        val session = mediaSession ?: return
        val player = session.player
        player.clearMediaItems()
        uris.forEach { uri ->
            player.addMediaItem(MediaItem.fromUri(uri))
        }
        player.prepare()
        player.seekTo(startIndex, 0)
        player.play()
    }

    companion object {
        @Volatile
        var currentAudioSessionId: Int = 0
            private set

        @Volatile
        var skipSilenceEnabled: Boolean = false

        @Volatile
        var pendingCrossfadeDurationMs: Int = 0
    }
}
