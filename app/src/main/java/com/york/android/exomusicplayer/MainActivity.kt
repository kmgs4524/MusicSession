package com.york.android.exomusicplayer

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.AudioCapabilities
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import kotlinx.android.synthetic.main.activity_main.*

import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.util.Util.getUserAgent
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.custom_control.*
import java.net.URI


class MainActivity : AppCompatActivity() {
    // verify permission
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val filePath = Uri.parse("/storage/emulated/0/Music/像天堂的懸崖.mp3")
    val bandwidthMeter = DefaultBandwidthMeter()
    val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
    var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    fun init() {
        verifyStoragePermission()
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)

        // Bind the player to the view.
        playerView_main.player = player

        // Measures bandwidth during playback. Can be null if not required.
        val bandwidthMeter = DefaultBandwidthMeter()
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter)
        // This is the MediaSource representing the media to be played.
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(filePath)
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
                        runOnUiThread {
                            exo_position.setText("${positionSeconds}")
                        }
                    }
                })
            }

        })
    }

    fun verifyStoragePermission() {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

}
