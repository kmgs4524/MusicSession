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

    val filePath = Uri.parse("/storage/emulated/0/Music/像天堂的懸崖.mp3")
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

    fun setUiHandler() {

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("PlayService", "onUnBind: intent: ${intent}")
        return true
    }

    fun createConcatenatingMediaSource(songs: List<Song>) {
        this.songs = songs
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter)


        songs.forEach {
            var videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                    .setExtractorsFactory(DefaultExtractorsFactory())
                    .createMediaSource(Uri.parse(it.filePath))
            dynamicConcatenatingMediaSource.addMediaSource(videoSource)
        }

        // Prepare the player with the source.
        if (player == null) {
            Log.d("PlayService", "player is null")
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
            setPlayerListener()
            player?.prepare(dynamicConcatenatingMediaSource)
            player?.playWhenReady = false
            Log.d("PlayService", "currentWindowIndex: ${player?.currentWindowIndex}")
        }
    }

    fun setPlayerListener() {
        player?.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

            }

            override fun onSeekProcessed() {

            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
//                Log.d("PlayService", "onTracksChanged trackGroups: ${trackGroups?.get(0)?.length}, trackSelections: ${trackSelections?.get(0)}")
            }

            override fun onPlayerError(error: ExoPlaybackException?) {

            }

            override fun onLoadingChanged(isLoading: Boolean) {

            }

            // called every time the video changes or "seeks" to the next in the playlist.
            override fun onPositionDiscontinuity(reason: Int) {
                Log.d("playerWindow", "player?.currentWindowIndex: ${player?.currentWindowIndex} song: ${songs[player?.currentWindowIndex!!]}" +
                        "name: ${songs[player?.currentWindowIndex!!].name} artist: ${songs[player?.currentWindowIndex!!].artist}")
                val message = Message()

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
                Log.d("onTimelineChanged", "timeline: ${timeline}")
            }

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if(playbackState == Player.STATE_READY) {
                    val statusMessage = Message()
                    val statusBundle = Bundle()

                    statusBundle.putBoolean("IS_PLAYING", playWhenReady)
                    statusMessage.data = statusBundle
                    statusHandler.sendMessage(statusMessage)
                }

                Log.d("onPlayerStateChanged", "playbackState: ${playbackState} playWhenReady: ${playWhenReady}")

                if (playbackState == Player.STATE_READY) {
//                    bundle.putBoolean("IS_PLAYING", player?.playbackState)
                    // put duration
                    bundle.putInt("DURATION", player?.duration!!.toInt() / 1000)

                    if (handlerThread.state == Thread.State.NEW) {
                        handlerThread.start()
                    } else if (handlerThread.state == Thread.State.RUNNABLE) {
                        Log.d("onPlayerStateChanged", "thread state: ${handlerThread.state}")
                        currentPositionSeconds = 0
                    }
                    // currentPositionSeconds that put in Runnable would not work, I still don't know why.
                    val runnable = Runnable {
                        Log.d("thread check", "current thread id: ${Thread.currentThread().id}")

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
        player?.seekTo(player!!.previousWindowIndex, 0)
    }

    fun playNext() {
        player?.seekTo(player!!.nextWindowIndex, 0)
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
