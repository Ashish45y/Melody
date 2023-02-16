package dev.ashish.melody.exoplayer.callbacks

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import dev.ashish.melody.exoplayer.MusicService

class MusicPlayerNotificationListener(
    private val musicService: MusicService):
        PlayerNotificationManager.NotificationListener {
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        musicService.apply {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        musicService.apply {
            if(ongoing && !isForegroundService){
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext,this::class.java)
                )
                startForeground(notificationId,notification)
                isForegroundService = true
            }
        }
    }
}
