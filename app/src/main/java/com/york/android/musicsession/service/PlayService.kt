package com.york.android.musicsession.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.app.PendingIntent
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
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

    var songs: List<Song> = ArrayList<Song>()
    var songsSize: Int = 0

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
//        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PlayService", "onStartCommand intent: ${intent}")

        return super.onStartCommand(intent, flags, startId)
    }

    fun setUiHandler() {

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("PlayService", "onUnBind: intent: ${intent}")
        return true
    }

    fun createConcatenatingMediaSource(songs: List<Song>) {
        this.songs = songs
        Log.d("PlayService", "createConcatenatingMediaSource songs size: ${songs.size}")
//        this.songs = songs
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter)

        // Prepare the player with the source.
        if (player == null) {
            Log.d("PlayService", "player is null")
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
            songs.forEach {
                var videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
//                        .setExtractorsFactory(DefaultExtractorsFactory())
                        .createMediaSource(Uri.parse(it.filePath))
                dynamicConcatenatingMediaSource.addMediaSource(videoSource)
//                Log.d("PlayService", "createConcatenatingMediaSource song: ${it.name}")
            }
            Log.d("PlayService", "dynamicConcatenatingMediaSource size: ${dynamicConcatenatingMediaSource.size}")
            player?.prepare(dynamicConcatenatingMediaSource)
            Log.d("PlayService", "songSize: ${songsSize}")
            setPlayerListener()
            player?.shuffleModeEnabled = false
            player?.setRepeatMode(Player.REPEAT_MODE_ALL)
            Log.d("PlayService", "currentWindowIndex: ${player?.currentWindowIndex}")
        } else {
            Log.d("PlayService", "player not null")
            // remove existing media sources
            val playListSize = dynamicConcatenatingMediaSource.size

            songs.forEach {
                var videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                        .setExtractorsFactory(DefaultExtractorsFactory())
                        .createMediaSource(Uri.parse(it.filePath))
                dynamicConcatenatingMediaSource.addMediaSource(videoSource)
                Log.d("PlayService", "createConcatenatingMediaSource song: ${it.name}")
            }
//            val currentSource = dynamicConcatenatingMediaSource.moveMediaSource(player?.currentWindowIndex!!, 0)
            for (i in 0 until playListSize) {
                dynamicConcatenatingMediaSource.removeMediaSource(0)
            }
//            Log.d("PlayService", "index: 0 element: ${dynamicConcatenatingMediaSource.getMediaSource(0)} " +
////                    "index: 1 element: ${dynamicConcatenatingMediaSource.getMediaSource(1)} " +
//                    "currentWindowIndex: ${player?.currentWindowIndex!!}" +
//                    "current media: ${dynamicConcatenatingMediaSource.getMediaSource(player?.currentWindowIndex!!)}" +
//                    "currentPeriodIndex ${player?.currentPeriodIndex}" +
//                    "current media: ${dynamicConcatenatingMediaSource.getMediaSource(player?.currentPeriodIndex!!)}")
//            Log.d("PlayService", "index 0 element: ${dynamicConcatenatingMediaSource.getMediaSource(0)} " +
//                    "currentWindowIndex: ${player?.currentWindowIndex!!}" +
//                    "current media: ${dynamicConcatenatingMediaSource.getMediaSource(player?.currentWindowIndex!!)}" +
//                    "currentPeriodIndex ${player?.currentPeriodIndex}" +
//                    "current media: ${dynamicConcatenatingMediaSource.getMediaSource(player?.currentPeriodIndex!!)}")

            Log.d("PlayService", "playList size: ${dynamicConcatenatingMediaSource.size}")
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

            }

            override fun onLoadingChanged(isLoading: Boolean) {

            }

            // called every time the video changes or "seeks" to the next in the playlist.
            override fun onPositionDiscontinuity(reason: Int) {
                Log.d("PlayService", "onPositionDiscontinuity player?.currentWindowIndex: ${player?.currentWindowIndex} player?.currentPeriodIndex: ${player?.currentPeriodIndex}" +
                        "song: ${songs[player?.currentWindowIndex!!]}" +
                        "name: ${songs[player?.currentWindowIndex!!].name} artist: ${songs[player?.currentWindowIndex!!].artist}")
                val message = Message()

                Log.d("PlayService", "song artist: ${songs[player?.currentPeriodIndex!!].artist}")
                infoBundle.putString("ARTIST_NAME", songs[player?.currentWindowIndex!!].artist)
                infoBundle.putString("SONG_NAME", songs[player?.currentWindowIndex!!].name)
                message.data = infoBundle
                infoHandler.sendMessage(message)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

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
//                        Log.d("thread check", "current thread id: ${Thread.currentThread().id}")

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
        // Produces DataSource instances through which media data is loaded.
        Log.d("PlayService", "player: ${player}")
        player?.seekTo(index, 0)
        Log.d("PlayService", "currentWindowIndex: ${player?.currentWindowIndex}")
        Log.d("PlayService", "dynamicConcatenatingMediaSource: ${dynamicConcatenatingMediaSource.size}")
        Log.d("PlayService", "dynamicConcatenatingMediaSource: ${dynamicConcatenatingMediaSource.getMediaSource(player?.currentWindowIndex!!)}")
        // Prepare the player with the source.
        player?.playWhenReady = true
    }

    fun display() {
        player?.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun playPrevious() {
        Log.d("playPrevious", "windowIndex; ${player?.currentWindowIndex} prev Index: ${player?.previousWindowIndex}")
        player?.seekToDefaultPosition(player!!.previousWindowIndex)
    }

    fun playNext() {
        Log.d("playNext", "windowIndex; ${player?.currentWindowIndex} next index: ${player?.nextWindowIndex}")
//        player?.seekTo(player!!.nextWindowIndex, C.TIME_UNSET)
        player?.seekToDefaultPosition(player!!.nextWindowIndex)
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
