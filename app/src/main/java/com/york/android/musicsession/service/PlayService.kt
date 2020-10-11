package com.york.android.musicsession.service

import android.content.Intent
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
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
import timber.log.Timber
import java.util.*


class PlayService : MediaBrowserServiceCompat() {

    // Measures bandwidth during playback. Can be null if not required.
    private val bandwidthMeter = DefaultBandwidthMeter()
    private val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    private val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
    private var player: ExoPlayer? = null

    private val dynamicConcatenatingMediaSource = DynamicConcatenatingMediaSource() // playlist that store media sources

    private val songMetadataBuilder = MediaMetadataCompat.Builder()
    private val stateBuilder = PlaybackStateCompat.Builder()

    private var songs: List<Song> = ArrayList() // store received songs
    private var currentWindowIndex = 0
    private var isClickedFromDirectory = false
    private var isRandom = false

    private lateinit var mediaSession: MediaSessionCompat
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            extras?.let {
                it.classLoader = Song::class.java.classLoader
                val songs = it.getParcelableArrayList<Song>("SONGS") ?: return@let
                createMediaSource(songs)

                currentWindowIndex = it.getInt("CURRENT_WINDOW_INDEX")
                playMediaSource(currentWindowIndex)

                Timber.d( "onPlayFromUri uri: $uri hashcode: ${it.hashCode()} extras: $it")
                Timber.d( "currentWindowIndex: $currentWindowIndex")
            }

            // controller for the session
            val controller = mediaSession.controller
            val creator = PlayerNotificationCreator(this@PlayService, mediaSession.sessionToken, controller)
            controller.registerCallback(object : MediaControllerCompat.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                    changeNotificationMetadata(creator, metadata)
                }

                override fun onPlaybackStateChanged(playbackStateCompat: PlaybackStateCompat) {
                    changeNotificationState(creator, playbackStateCompat)
                }
            })
        }

        override fun onPlay() = display()

        override fun onPause() = pause()

        override fun onSkipToNext() = playNext()

        override fun onSkipToPrevious() = playPrevious()

        override fun onSeekTo(pos: Long) {
            isClickedFromDirectory = true

            val handler = Handler()
            handler.postDelayed({
                player?.seekTo(pos * 1000)
            }, 500)
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                isRandom = true
                mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
            } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                isRandom = false
                mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val isTranslated = super.onMediaButtonEvent(mediaButtonEvent)
            val keyCode = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)?.keyCode ?: return false
            when (keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> onPlay()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> onPause()
                KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
            }
            return isTranslated
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(applicationContext, PlayService::class.java.simpleName)
        this.sessionToken = mediaSession.sessionToken
        initMediaSession()
    }

    private fun initMediaSession() {
        // connect MediaBrowserService to MediaSession
        mediaSession.isActive = true
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val defaultPlaybackState = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                .setState(PlaybackStateCompat.STATE_PLAYING,
                        0,
                        1f)
        mediaSession.setPlaybackState(defaultPlaybackState.build())
        mediaSession.setCallback(mediaSessionCallback)
        mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("MusicSession", null)
    }

    fun changeNotificationMetadata(creator: PlayerNotificationCreator, mediaMetadata: MediaMetadataCompat) {
        creator.setMetadata(mediaMetadata)
        val notification = creator.create()
        startForeground(FOREGROUND_SERVICE_ID, notification)
    }

    fun changeNotificationState(creator: PlayerNotificationCreator, state: PlaybackStateCompat) {
        creator.setState(state)
        val notification = creator.create()
        startForeground(FOREGROUND_SERVICE_ID, notification)
    }

    private fun createMediaSource(songs: List<Song>) {
        this.songs = songs
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "MusicSession"), bandwidthMeter)
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(applicationContext, trackSelector)
            setPlayerListener()
            songs.forEach {
                val musicSource = ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(it.filePath))
                dynamicConcatenatingMediaSource.addMediaSource(musicSource)
            }
            player?.repeatMode = Player.REPEAT_MODE_ALL
            player?.prepare(dynamicConcatenatingMediaSource)
        } else {
            val playListSize = dynamicConcatenatingMediaSource.size
            // 移除 playlist 中所有的 media source
            // 內部是用 ArrayList 實作，每執行一次 remove 所有項目就會往前移一位，故每次都要從頭開始刪
            for (i in 0 until playListSize) {
                dynamicConcatenatingMediaSource.removeMediaSource(0)
            }
            // 重新加入當前目錄的所有歌
            songs.forEach {
                val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                        .setExtractorsFactory(DefaultExtractorsFactory())
                        .createMediaSource(Uri.parse(it.filePath))
                dynamicConcatenatingMediaSource.addMediaSource(videoSource)
            }
        }
    }

    private fun setPlayerListener() {
        player?.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

            override fun onSeekProcessed() {}

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

            override fun onPlayerError(error: ExoPlaybackException?) {}

            override fun onLoadingChanged(isLoading: Boolean) {}

            // called every time the video changes or "seeks" to the next in the playlist.
            override fun onPositionDiscontinuity(reason: Int) {
                if (!isClickedFromDirectory) {
                    if (currentWindowIndex == songs.size - 1) {
                        currentWindowIndex = 0
                    } else {
                        currentWindowIndex++
                    }
                }
                Timber.d( "isClicked: $isClickedFromDirectory currentWindowIndex: $currentWindowIndex " +
                        "duration: ${player?.duration}")
                songMetadataBuilder.putString("ARTIST_NAME", songs[currentWindowIndex].artist)
                        .putString("SONG_NAME", songs[currentWindowIndex].name)
                        .putString("ALBUM_ARTWORK", songs[currentWindowIndex].coverImageUrl)
                        .putLong("DURATION", player?.duration!! / 1000)
                mediaSession.setMetadata(songMetadataBuilder.build())

                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        player?.currentPosition!!,
                        1f)
                mediaSession.setPlaybackState(stateBuilder.build())

                isClickedFromDirectory = false
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                Timber.d( "onShuffleModeEnabledChanged shuffle mode: $shuffleModeEnabled")
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Timber.d( "playbackState: $playbackState playWhenReady: $playWhenReady windowIndex: ${player?.currentWindowIndex}")

                if (playbackState == Player.STATE_READY && playWhenReady) {
                    // put duration
                    // If song is ready to play, put song's metadata to MediaSession
                    songMetadataBuilder.putLong("DURATION", player?.duration!! / 1000)
                    mediaSession.setMetadata(songMetadataBuilder.build())
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, player?.currentPosition!!, 1f)
                    mediaSession.setPlaybackState(stateBuilder.build())
                    Timber.d( "start playing")
                } else {
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, player?.currentPosition!!, 0f)
                    mediaSession.setPlaybackState(stateBuilder.build())
                    Timber.d( "stop playing")
                }
            }
        })
    }

    fun playMediaSource(index: Int) {
        currentWindowIndex = index
        isClickedFromDirectory = true
        // Produces DataSource instances through which media data is loaded.
        Timber.d( "playMediaSource index : $index")
        val handler = Handler()
        handler.postDelayed({
            player?.seekTo(index, 0)
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
        isClickedFromDirectory = true
        if (isRandom) {
            currentWindowIndex = randomize(0, songs.size - 1)
        } else {
            if (currentWindowIndex != 0) {
                currentWindowIndex -= 1
            } else {
                currentWindowIndex = songs.size - 1
            }
        }
        Timber.d( "isRandom: $isRandom playPrevious currentWindowIndex: $currentWindowIndex")
        player?.seekTo(currentWindowIndex, 0)
    }

    fun playNext() {
        isClickedFromDirectory = true
        if (isRandom) {
            currentWindowIndex = randomize(0, songs.size - 1)
        } else {
            if (currentWindowIndex < songs.size - 1) {
                currentWindowIndex += 1
            } else {
                currentWindowIndex = 0
            }
        }
        Timber.d( "isRandom: $isRandom playNext currentWindowIndex: $currentWindowIndex")
        player?.seekTo(currentWindowIndex, 0)
    }

    private fun randomize(from: Int, to: Int): Int {
        val random = Random()
        return random.nextInt(to - from) + from
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        player?.release()
    }

    companion object {
        const val FOREGROUND_SERVICE_ID = 943
    }
}
