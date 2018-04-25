package com.york.android.musicsession.service

import android.content.ComponentName
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.view.notification.PlayerNotificationCreator
import java.util.*


class PlayService : MediaBrowserServiceCompat() {
    // Measures bandwidth during playback. Can be null if not required.
    val bandwidthMeter = DefaultBandwidthMeter()
    val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
    var player: ExoPlayer? = null

    val dynamicConcatenatingMediaSource = DynamicConcatenatingMediaSource() // playlist that store media sources

    val songMetadataBuilder = MediaMetadataCompat.Builder()
    val stateBuilder = PlaybackStateCompat.Builder()

    var songs: List<Song> = ArrayList() // store received songs
    var currentWindowIndex = 0

    var handlerThread = HandlerThread("HandlerThread")  // handle player's current position

    // components for MediaSession service side
    lateinit var mediaSession: MediaSessionCompat
    // transport controls will invoke play, puase, etc actions on the connectionCallback
    val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            Log.d("playSong", "onPlayFromUri uri: ${uri} hashcode: ${extras?.hashCode()} extras: ${extras}")
            extras?.classLoader = Song::class.java.classLoader
            currentWindowIndex = extras?.getInt("CURRENT_WINDOW_INDEX")!!
            Log.d("playSong", "currentWindowIndex: ${currentWindowIndex}")
            createMediaSource(extras?.getParcelableArrayList<Song>("SONGS")!!)
            playMediaSource(currentWindowIndex)

            val controller = mediaSession.controller
//            val metaData = controller.metadata

            controller.registerCallback(object : MediaControllerCompat.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                    super.onMetadataChanged(metadata)
                    Log.d("Controller.Callback", "metadata: ${metadata} description: ${metadata?.description}")
                    setNotification(controller, metadata!!)
                }

                override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                    super.onPlaybackStateChanged(state)
                    setNotification(controller, metadata!!)
                }
            })

        }

        override fun onPlay() {
            super.onPlay()
            display()
            Log.d("MediaSession Callback", "play event")
        }

        override fun onPause() {
            super.onPause()
            pause()
            Log.d("MediaSession Callback", "pause event")
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            Log.d("MediaSession Callback", "onSkipToNext ")
            playNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            Log.d("MediaSession Callback", "onSkipToPrevious")
            playPrevious()
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            val isTranslated = super.onMediaButtonEvent(mediaButtonEvent)
            Log.d("onMediaButtonEvent", "isTranslated ${isTranslated}")
            Log.d("onMediaButtonEvent", "keyEvent ${mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)}" +
                    "keyCode: ${mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)?.keyCode}")
            val keyCode = mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)?.keyCode
            when(keyCode) {
                KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
            }
            return isTranslated
        }

    }



    lateinit var runnable: Runnable

//    override fun onBind(intent: Intent): IBinder? {
//        Log.d("PlayService", "onBind: ")
//        return LocalBinder()
//    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate() {
        super.onCreate()
        // connect MediaBrowserService to MediaSession
        //
        mediaSession = MediaSessionCompat(this, PlayService::class.java.simpleName,
                ComponentName(this, MediaButtonReceiver::class.java), null)
        // MediaSession's token is used create a MediaControllerCompat for interacting with this session
        // In order to let connecting component get token from service, service should set token as soon as possible
        this.sessionToken = mediaSession.sessionToken
        //
        mediaSession.isActive = true
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val defaultPlaybackState = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT)    // PlaybackStateCompat.ACTION_PAUSE and PlaybackStateCompat.ACTION_SKIP_TO_NEXT and
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
        mediaSession.setPlaybackState(defaultPlaybackState.build() )
        mediaSession.setCallback(mediaSessionCallback)
        Log.d("PlayService", "onCreate isActive: ${mediaSession.isActive} ")
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("MusicSession", null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Receiver intent
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("PlayService", "onUnBind: intent: ${intent}")
        return true
    }

    fun setNotification(controller: MediaControllerCompat, mediaMetadata: MediaMetadataCompat) {
        val FOREGROUND_SERVICE_ID = 943
        val creator = PlayerNotificationCreator(this, mediaSession.sessionToken, controller, mediaMetadata)
        val notification = creator.create()
        startForeground(FOREGROUND_SERVICE_ID, notification)
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
                Log.d("PlayService", "onPositionDiscontinuity duration: ${player?.duration!! / 1000}")
                songMetadataBuilder.putString("ARTIST_NAME", songs[currentWindowIndex].artist)
                        .putString("SONG_NAME", songs[currentWindowIndex].name)
                        .putString("ALBUM_ARTWORK", songs[currentWindowIndex].coverImageUrl)
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
                Log.d("onPlayerStateChanged", "playbackState: ${playbackState} playWhenReady: ${playWhenReady} windowIndex: ${player?.currentWindowIndex}")

                // start handlerThread if it is new
                if (handlerThread.state == Thread.State.NEW) {
                    handlerThread.start()
                }
                val handler = Handler(handlerThread.looper)
                if (playbackState == Player.STATE_READY && playWhenReady == true) {
                    // put duration
                    // If song is ready to play, put song's metadata to MediaSession
                    songMetadataBuilder.putLong("DURATION", player?.duration!! / 1000)
                    mediaSession.setMetadata(songMetadataBuilder.build())

                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, player?.currentPosition!!, 1f)
                    mediaSession.setPlaybackState(stateBuilder.build())
                    Log.d("onPlayerStateChanged", "start playing")
                } else {
//                    currentPositionUpdateHandler.removeCallbacks(runnable)
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, player?.currentPosition!!, 0f)
                    mediaSession.setPlaybackState(stateBuilder.build())
                    Log.d("onPlayerStateChanged", "stop playing")
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
//        mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 0f).create())
    }

    fun pause() {
        Log.d("PlayService", "pause")
        player?.playWhenReady = false
//        mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 0f).create())
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
        mediaSession.release()
    }

    inner class LocalBinder : Binder() {
        fun getService(): PlayService {
            return this@PlayService
        }
    }
}
