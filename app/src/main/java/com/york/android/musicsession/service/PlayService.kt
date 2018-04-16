package com.york.android.musicsession.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.app.PendingIntent
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.view.exoplayer.AlbumActivity
import java.util.*


class PlayService : Service() {
    val ONGOING_NOTIFICATION_ID = 1

    // Measures bandwidth during playback. Can be null if not required.
    val bandwidthMeter = DefaultBandwidthMeter()
    val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
    var player: ExoPlayer? = null

    val dynamicConcatenatingMediaSource = DynamicConcatenatingMediaSource()

    lateinit var timeHandler: Handler  // responsible for updating progressbar
    lateinit var infoHandler: Handler
    lateinit var statusHandler: Handler
    val bundle = Bundle()   // package including media's duration and current position
    val infoBundle = Bundle()
    var currentPositionSeconds: Int = 0

    var songs: List<Song> = ArrayList()
    var currentWindowIndex = 0

    var handlerThread = HandlerThread("HandlerThread")

    override fun onBind(intent: Intent): IBinder? {
        Log.d("PlayService", "onBind: ")
        return LocalBinder()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate() {
        val notificationIntent = Intent(applicationContext, AlbumActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)
        // create notification
        val notification = Notification.Builder(applicationContext)
                .setContentTitle("音樂播放")
                .setContentText("正在播放歌曲")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.exo_controls_play)
                .build()

        Log.d("PlayService", "onCreate notification: ${notification}")
//        val notification = Notification(R.drawable.exo_controls_play, "音樂播放",
//                System.currentTimeMillis())
        // start foreground service
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

    fun createMediaSource(songs: List<Song>) {
        this.songs = songs
        Log.d("PlayService", "createMediaSource songs size: ${songs.size}")
//        this.songs = songs
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter)

        // Prepare the player with the source.
        if (player == null) {
            Log.d("PlayService", "player is null")
            player = ExoPlayerFactory.newSimpleInstance(applicationContext, trackSelector)
            setPlayerListener()
            songs.forEach {
                var videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
//                        .setExtractorsFactory(DefaultExtractorsFactory())
                        .createMediaSource(Uri.parse(it.filePath))
                dynamicConcatenatingMediaSource.addMediaSource(videoSource)
//                Log.d("PlayService", "createMediaSource song: ${it.name}")
            }
            Log.d("PlayService", "dynamicConcatenatingMediaSource size: ${dynamicConcatenatingMediaSource.size}")
            player?.setRepeatMode(Player.REPEAT_MODE_ALL)
            player?.prepare(dynamicConcatenatingMediaSource)
            Log.d("PlayService", "currentWindowIndex: ${player?.currentWindowIndex}")
        } else {
            Log.d("createMediaSource", "player not null")
            // remove existing media sources
            val playListSize = dynamicConcatenatingMediaSource.size

            songs.forEach {
                var videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                        .setExtractorsFactory(DefaultExtractorsFactory())
                        .createMediaSource(Uri.parse(it.filePath))
                dynamicConcatenatingMediaSource.addMediaSource(videoSource)
//                Log.d("PlayService", "createMediaSource song: ${it.name}")
            }

            for (i in 0 until playListSize) {
                dynamicConcatenatingMediaSource.removeMediaSource(0)
            }

        }

    }

    fun setPlayerListener() {
        player?.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

            }

            override fun onSeekProcessed() {
                Log.d("PlayService", "onSeekProcessed currentWindowIndex: ${player?.currentWindowIndex}, periodIndex: ${player?.currentPeriodIndex}")
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                Log.d("PlayService", "onTracksChanged currentWindowIndex: ${player?.currentWindowIndex}, periodIndex: ${player?.currentPeriodIndex}")
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Log.d("PlayService", "onPlayerError ${error}")
            }

            override fun onLoadingChanged(isLoading: Boolean) {
//                Log.d("PlayService", "onLoadingChanged isLoading: ${isLoading}")
            }

            // called every time the video changes or "seeks" to the next in the playlist.
            override fun onPositionDiscontinuity(reason: Int) {
                Thread(Runnable {
                    //                    val currentWindowIndex = player?.currentWindowIndex
//                    val periodWindowIndex = player?.currentPeriodIndex
                    Log.d("PlayService", "onPositionDiscontinuity currentWindowIndex: ${currentWindowIndex} currentPeriodIndex: ${player?.currentPeriodIndex}")
                    val message = Message()

                    infoBundle.putString("ARTIST_NAME", songs[currentWindowIndex].artist)
                    infoBundle.putString("SONG_NAME", songs[currentWindowIndex].name)
                    infoBundle.putString("ALBUM_ARTWORK", songs[currentWindowIndex].coverImageUrl)
                    message.data = infoBundle
                    infoHandler.sendMessage(message)
                }).run()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                Log.d("PlayService", "onShuffleModeEnabledChanged shuffle mode: ${shuffleModeEnabled}")
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                Log.d("PlayService", "onTimelineChanged player.currentWindowIndex: ${player?.currentWindowIndex} currentPeriodIndex: ${player?.currentPeriodIndex}")
            }

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val statusMessage = Message()
                    val statusBundle = Bundle()

                    statusBundle.putBoolean("IS_PLAYING", playWhenReady)
                    statusMessage.data = statusBundle
                    statusHandler.sendMessage(statusMessage)
                }

                Log.d("onPlayerStateChanged", "playbackState: ${playbackState} playWhenReady: ${playWhenReady} windowIndex: ${player?.currentWindowIndex}")

                if (playbackState == Player.STATE_READY) {
//                    bundle.putBoolean("IS_PLAYING", player?.playbackState)
                    // put duration
                    try {
                        bundle.putInt("DURATION", player?.duration!!.toInt() / 1000)
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }

                    if (handlerThread.state == Thread.State.NEW) {
                        handlerThread.start()
                    } else if (handlerThread.state == Thread.State.RUNNABLE) {
//                        Log.d("onPlayerStateChanged", "thread state: ${handlerThread.state}")
                        currentPositionSeconds = 0
                    }
                    // currentPositionSeconds that put in Runnable would not work, I still don't know why.
                    val runnable = Runnable {
                        // Log.d("thread check", "current thread id: ${Thread.currentThread().id}")

                        while (playbackState == Player.STATE_READY) {
                            if (player?.currentPosition!! / 1000 > currentPositionSeconds) {
                                currentPositionSeconds = player?.currentPosition!!.toInt() / 1000

                                bundle.putInt("CURRENT_POSITION", currentPositionSeconds)

                                val message = timeHandler.obtainMessage()
                                message.data = bundle

                                val placedSuccess = timeHandler.sendMessageAtTime(message, 100)
                            }

                        }
                    }

                    val handler = Handler(handlerThread.looper)

                    handler.post(runnable)
                }

            }
        })
    }

    fun playMediaSource(index: Int) {
        currentWindowIndex = index
        // Produces DataSource instances through which media data is loaded.
        Log.d("PlayService", "playMediaSource index : ${index}")
        val handler = Handler()
        handler.postDelayed(Runnable {
            player?.seekTo(index, 0)
            Log.d("PlayService", "playMediaSource playMediaSource currentWindowIndex: ${player?.currentWindowIndex}")
//        Log.d("PlayService", "dynamicConcatenatingMediaSource: ${dynamicConcatenatingMediaSource.size}")
            Log.d("PlayService", "playMediaSource dynamicConcatenatingMediaSource: ${dynamicConcatenatingMediaSource.getMediaSource(player?.currentWindowIndex!!)}")
            // Prepare the player with the source.
            player?.playWhenReady = true
        }, 500)

    }

    fun display() {
        player?.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun playPrevious() {
        if (currentWindowIndex != 0) {
            currentWindowIndex = currentWindowIndex - 1
        } else {
            currentWindowIndex = songs.size - 1
        }

//        Log.d("playPrevious", "currentWindowIndex; ${currentWindowIndex} ")
        Log.d("playNext", "nextWindowIndex; ${player?.previousWindowIndex}")
        player?.seekToDefaultPosition(currentWindowIndex)
    }

    fun playNext() {
//        val currentPeriodIndex = player?.currentPeriodIndex
        val handler = Handler()
//        handler.postDelayed(Runnable {
        if (currentWindowIndex < songs.size - 1) {
            currentWindowIndex = currentWindowIndex + 1
        } else {
            currentWindowIndex = 0
        }

//            Log.d("playNext", "currentWindowIndex; ${currentWindowIndex}")

//        player?.seekTo(player!!.nextWindowIndex, C.TIME_UNSET)
        player?.seekToDefaultPosition(currentWindowIndex)

//        }, 500)

    }

    override fun onDestroy() {
        super.onDestroy()
        (player as SimpleExoPlayer).release()
    }

    inner class LocalBinder : Binder() {
        fun getService(): PlayService {
            return this@PlayService
        }
    }

}
