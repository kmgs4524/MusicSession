package com.york.android.exomusicplayer.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.PendingIntent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.york.android.exomusicplayer.R
import com.york.android.exomusicplayer.model.Song
import com.york.android.exomusicplayer.view.MainActivity


class PlayService : Service() {

//    constructor(player: ExoPlayer): this() {
//        this.player = player
//    }

    val ONGOING_NOTIFICATION_ID = 1

    val filePath = Uri.parse("/storage/emulated/0/Music/像天堂的懸崖.mp3")
    // Measures bandwidth during playback. Can be null if not required.
    val bandwidthMeter = DefaultBandwidthMeter()
    val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
    var player: ExoPlayer? = null

    override fun onBind(intent: Intent): IBinder? {
        Log.d("PlayService", "onBind: ")
        return LocalBinder()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate() {
//        initExoPlayer()

        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)
        val notification = Notification.Builder(applicationContext)
                .setContentTitle("音樂播放")
                .setContentText("正在播放歌曲")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.exo_controls_play)
                .build()

        Log.d("PlayService", "onCreate notification: ${notification}")
//        val notification = Notification(R.drawable.exo_controls_play, "音樂播放",
//                System.currentTimeMillis())

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PlayService", "onStartCommand intent: ${intent}")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("PlayService", "onUnBind: intent: ${intent}")
        return true
    }

    fun initExoPlayer(song: Song) {
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter)
        // This is the MediaSource representing the media to be played.
//        val uri = Uri.parse(song.filePath)
        val uri = Uri.parse("${song.filePath}")
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        Log.d("init", "song path: ${song.filePath}")
        Log.d("init", "videoSource: ${videoSource}")
        // Prepare the player with the source.
        player?.prepare(videoSource)

        player?.playWhenReady = true

        play()
    }

    fun play() {
        player?.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

            }

            override fun onSeekProcessed() {

            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

            }

            override fun onPlayerError(error: ExoPlaybackException?) {

            }

            override fun onLoadingChanged(isLoading: Boolean) {

            }

            override fun onPositionDiscontinuity(reason: Int) {

            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Log.d("onPlayerStateChanged", "playbackState: ${playbackState}")
                Thread(Runnable {
                    while(player?.playbackState == Player.STATE_READY) {
                        var positionSeconds = player!!.currentPosition / 1000
                        Log.d("onPlayerStateChanged", "positionSeconds: ${positionSeconds}")
//                        runOnUiThread {
//                            exo_position.setText("${positionSeconds}")
//                        }
                    }
                })
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        (player as SimpleExoPlayer).release()
    }

    inner class LocalBinder: Binder() {
        fun getService(): PlayService {
            return this@PlayService
        }
    }

}
