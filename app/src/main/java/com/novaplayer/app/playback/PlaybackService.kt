package com.novaplayer.app.playback

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.novaplayer.app.MainActivity
import com.novaplayer.app.NovaPlayerApplication
import com.novaplayer.app.R

@UnstableApi
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()

        startForeground(
            NovaPlayerApplication.PLAYBACK_NOTIFICATION_ID,
            createNotification()
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            flags
        )

        return NotificationCompat.Builder(this, NovaPlayerApplication.PLAYBACK_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.now_playing))
            .setSmallIcon(R.drawable.ic_nova_player)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
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
}

