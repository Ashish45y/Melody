package dev.ashish.melody.exoplayer

import android.support.v4.media.session.PlaybackStateCompat

inline fun PlaybackStateCompat.isPrepared() =
    this.state == PlaybackStateCompat.STATE_BUFFERING ||
            this.state == PlaybackStateCompat.STATE_PLAYING ||
            this.state == PlaybackStateCompat.STATE_PAUSED