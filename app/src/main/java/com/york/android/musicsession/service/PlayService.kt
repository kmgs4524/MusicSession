package com.york.android.musicsession.service

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.*
import android.service.media.MediaBrowserService
import android.support.annotation.RequiresApi
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
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
import com.york.android.musicsession.boradcast.MusicReceiver
import com.york.android.musicsession.boradcast.SetBroadcastReceiver
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.view.notification.PlayerNotificationBuilder
import java.util.*


class PlayService : MediaBrowserServiceCompat() {
    companion object {
        val TRANSPORT_CONTROLS = 3134
    }

    // Measures bandwidth during playback. Can be null if not required.
    val bandwidthMeter = DefaultBandwidthMeter()
    val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
    var player: ExoPlayer? = null

    val dynamicConcatenatingMediaSource = DynamicConcatenatingMediaSource() // playlist that store media sources

//    lateinit var timeHandler: Handler  // responsible for updating progressbar
//    lateinit var infoHandler: Handler   // responsible for updating song information
//    lateinit var statusHandler: Handler
//    val bundle = Bundle()   // package including media's duration and current position
//    val infoBundle = Bundle()


    var songs: List<Song> = ArrayList() // store received songs
    var currentWindowIndex = 0

    var handlerThread = HandlerThread("HandlerThread")  // handle player's current position

    // components for MediaSession service side
    lateinit var mediaSession: MediaSessionCompat
    // transport controls will invoke play, puase, etc actions on the connectionCallback
    val callback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            Log.d("playSong", "onPlayFromUri uri: ${uri} hashcode: ${extras?.hashCode()} extras: ${extras}")
            extras?.classLoader = Song::class.java.classLoader
            currentWindowIndex = extras?.getInt("CURRENT_WINDOW_INDEX")!!
            Log.d("playSong", "currentWindowIndex: ${currentWindowIndex}")
            createMediaSource(extras?.getParcelableArrayList<Song>("SONGS")!!)
            playMediaSource(currentWindowIndex)
        }

        override fun onPlay() {
            display()
        }

        override fun onPause() {
            pause()
        }
    }

//    override fun onBind(intent: Intent): IBinder? {
//        Log.d("PlayService", "onBind: ")
//        return LocalBinder()
//    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate() {
        super.onCreate()
        // connect MediaBrowserService to MediaSession
        mediaSession = MediaSessionCompat(this, PlayService::class.java.simpleName)
        // MediaSession's token is used create a MediaControllerCompat for interacting with this session
        // In order to let connecting component get token from service, service should set token as soon as possible
        this.sessionToken = mediaSession.sessionToken
        mediaSession.setCallback(callback)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
//        songs = rootHints?.getParcelableArrayList<Song>("SONGS")!!
//        Log.d("onGetRoot", "Bundle: ${songs?.size}")
        return BrowserRoot("MusicSession", null)
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
//                Thread(Runnable {
//                    Log.d("PlayService", "onPositionDiscontinuity currentWindowIndex: ${currentWindowIndex} currentPeriodIndex: ${player?.currentPeriodIndex}")
//                    val message = Message()
//
//                    infoBundle.putString("ARTIST_NAME", songs[currentWindowIndex].artist)
//                    infoBundle.putString("SONG_NAME", songs[currentWindowIndex].name)
//                    infoBundle.putString("ALBUM_ARTWORK", songs[currentWindowIndex].coverImageUrl)
//                    message.data = infoBundle
//                    infoHandler.sendMessage(message)
//                }).run()
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
//                if (playbackState == Player.STATE_READY) {
//                    val statusMessage = Message()
//                    val statusBundle = Bundle()
//
//                    statusBundle.putBoolean("IS_PLAYING", playWhenReady)
//                    statusMessage.data = statusBundle
//                    statusHandler.sendMessage(statusMessage)
//                }

                Log.d("onPlayerStateChanged", "playbackState: ${playbackState} playWhenReady: ${playWhenReady} windowIndex: ${player?.currentWindowIndex}")

                if (playbackState == Player.STATE_READY) {
                    // put duration
//                    try {
//                        bundle.putInt("DURATION", player?.duration!!.toInt() / 1000)
//                    } catch (e: IndexOutOfBoundsException) {
//                        e.printStackTrace()
//                    }
//
//                    if (handlerThread.state == Thread.State.NEW) {
//                        handlerThread.start()
//                    } else if (handlerThread.state == Thread.State.RUNNABLE) {
////                        Log.d("onPlayerStateChanged", "thread state: ${handlerThread.state}")
//                        currentPositionSeconds = 0
//                    }
                    // currentPositionSeconds that put in Runnable would not work, I still don't know why.
                    handlerThread.start()
                    val runnable = Runnable {
                        while (playbackState == Player.STATE_READY) {
                            var currentPositionSeconds = 0L
                            if (player?.currentPosition!! / 1000 > currentPositionSeconds) {

                                currentPositionSeconds = player?.currentPosition!! / 1000

//                                bundle.putInt("CURRENT_POSITION", currentPositionSeconds)
//
//                                val message = timeHandler.obtainMessage()
//                                message.data = bundle
//
//                                val placedSuccess = timeHandler.sendMessageAtTime(message, 100)

                                // update playback's current state
                                val state = PlaybackStateCompat.Builder()
                                        .setState(PlaybackStateCompat.STATE_PLAYING, currentPositionSeconds, 1F)
                                        .build()
                                mediaSession.setPlaybackState(state)
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

        // create notification
//        val ONGOING_NOTIFICATION_ID = 1904
//        val notification = PlayerNotificationBuilder(applicationContext, songs[0])
//        startForeground(ONGOING_NOTIFICATION_ID, notification.show())
    }

    fun display() {
        player?.playWhenReady = true
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 0f).build())
    }

    fun pause() {
        Log.d("PlayService", "pause")
        player?.playWhenReady = false
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED, 0, 0f).build())
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
        val handler = Handler()

        if (currentWindowIndex < songs.size - 1) {
            currentWindowIndex = currentWindowIndex + 1
        } else {
            currentWindowIndex = 0
        }
        player?.seekToDefaultPosition(currentWindowIndex)
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
