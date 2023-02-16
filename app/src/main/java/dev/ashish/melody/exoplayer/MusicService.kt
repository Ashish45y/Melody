package dev.ashish.melody.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import dev.ashish.melody.exoplayer.callbacks.MusicPlaybackPrepare
import dev.ashish.melody.exoplayer.callbacks.MusicPlayerEventListener
import dev.ashish.melody.exoplayer.callbacks.MusicPlayerNotificationListener
import dev.ashish.melody.other.Constants.MEDIA_ROOT_ID
import dev.ashish.melody.other.Constants.NETWORK_ERROR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultMediaSourceFactory
    @Inject
    lateinit var exoPlayer: ExoPlayer
    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource
     private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    var isForegroundService = false
    private var curPlayingSong : MediaMetadataCompat? = null
    private var isPlayerInitialized = false
    private lateinit var musicPlayereEventListener : MusicPlayerEventListener

    companion object{
        var curSongDur = 0L
        private set
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch{
            firebaseMusicSource.fetchMediaData()
        }
      val activityIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken
        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            curSongDur = exoPlayer.duration
        }
        val musicPlaybackPrepare = MusicPlaybackPrepare(firebaseMusicSource){
            curPlayingSong= it
           preparePlayer(firebaseMusicSource.songs,it,true)
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPrepare)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)
        musicPlayereEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayereEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }
    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }
    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ){
        val curSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory = DefaultDataSourceFactory(this)))
        exoPlayer.seekTo(curSongIndex,0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }
    override  fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        exoPlayer.removeListener(musicPlayereEventListener)
        exoPlayer.release()
    }
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
       when(parentId){
           MEDIA_ROOT_ID -> {
               val resultSent = firebaseMusicSource.whenReady { isInitialized ->
                   if (isInitialized){
                       result.sendResult(firebaseMusicSource.asMediaItems())
                       if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()){
                           preparePlayer(firebaseMusicSource.songs,firebaseMusicSource.songs[0],false)
                           isPlayerInitialized = true
                       }

                   }else{
                       mediaSession.sendSessionEvent(NETWORK_ERROR,null)
                       result.sendResult(null)
                   }
               }
               if(!resultSent){
                   result.detach()
               }
           }
       }
    }
}