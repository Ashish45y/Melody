package dev.ashish.melody.exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.MediaDrm.PlaybackComponent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.ashish.melody.other.Constants.NETWORK_ERROR
import dev.ashish.melody.other.Event
import dev.ashish.melody.other.Resource

class MusicServiceConnection (context: Context) {
    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected : LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError : LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackComponent?>()
    val playbackState : LiveData<PlaybackComponent?> = _playbackState

    private val _currentPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentPlayingSong : LiveData<MediaMetadataCompat?> = _currentPlayingSong

    lateinit var mediaController: MediaControllerCompat
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context,MusicService::class.java),
        mediaBrowserConnectionCallback,null
    ).apply { connect() }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentId, callback)
    }
    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) : MediaBrowserCompat.
    ConnectionCallback(){
        override fun onConnected() {
               super.onConnected()
                mediaController = MediaControllerCompat(context,mediaBrowser.sessionToken).apply {
                     registerCallback(MediaControllerCallBack())
                }
              _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
           _isConnected.postValue(Event(Resource.error("The connection was suspended", false)))
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(Event(Resource.error("Couldn't connect to media browser", false)))
        }
    }

    private inner class MediaControllerCallBack: MediaControllerCompat.Callback(){
        fun onPlaybackStateChanged(state: PlaybackComponent?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
           _currentPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event){
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                    Resource.error("Could not connect to network check internet connection",
                    null)
                )
                )
            }
        }
        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}