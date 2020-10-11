package com.york.android.musicsession.view

import android.content.*
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomSheetBehavior
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.support.v4.app.FragmentTransaction
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.view.GravityCompat
import android.view.*
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.service.PlayService
import com.york.android.musicsession.view.playercontrol.PlayerControlDialogFragment
import com.york.android.musicsession.view.playercontrol.PlayerControlFragment
import com.york.android.musicsession.view.playercontrol.PlayerControlFragment.Companion.PLAY_CONTROL_FRAGMENT
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), PlayerControlFragment.OnFragmentInteractionListener,
        PlayerControlDialogFragment.Listener {

    private val bottomFragment = PlayerControlFragment.newInstance()
    private val discoverFragment = LibraryFragment.newInstance()

    private var service: PlayService? = null

    // MediaSession framework component
    private val connectionCallback = ConnectionCallback()
    private val controllerCallback = ControllerCallback()
    private lateinit var playbackState: PlaybackStateCompat
    private lateinit var playbackMetadata: MediaMetadataCompat
    private lateinit var mediaBrowser: MediaBrowserCompat

    private lateinit var songUri: Uri
    private var songs: List<Song> = ArrayList()
    private var currentWindowIndex = 0

    // 使用單一 background thread 定期更新播放的時間軸
    private val currentPositionExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val currentPositionUpdateHandler = Handler()

    // used to get the MediaController
    inner class ConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // MediaController give access to everything.
            val mediaController = MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
            // MediaController registers mediaSessionCallback to change UI when playback's stateBuilder changes.
            mediaController.registerCallback(controllerCallback)
            // set created MediaController in order to use it everywhere
            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
        }
    }

    fun setPlaylist(songs: List<Song>, index: Int) {
        this.songs = songs
        this.currentWindowIndex = index
        songUri = Uri.parse(songs[index].filePath)
        Timber.d( "setPlaylist songs $songs service: $service")

        // 如果正在播放則先暫停，否則播放下一首時會有中斷的雜音
        val playbackState = MediaControllerCompat.getMediaController(this).playbackState.state
        if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
            mediaController.transportControls.pause()
        }

        val bundle = Bundle().apply {
            putParcelableArrayList("SONGS", songs as ArrayList)
            putInt("CURRENT_WINDOW_INDEX", index)
        }
        MediaControllerCompat.getMediaController(this).transportControls.apply {
            playFromUri(songUri, bundle)
        }
        Timber.d( "uri: $songUri hashcode: ${bundle.hashCode()} bundle: $bundle ")
    }

    override fun onDisplaySong() {
        MediaControllerCompat.getMediaController(this).transportControls.apply {
            play()
        }
    }

    override fun onPauseSong() {
        MediaControllerCompat.getMediaController(this).transportControls.apply {
            pause()
        }
    }

    override fun onPlayPrevSong() {
        MediaControllerCompat.getMediaController(this).transportControls.apply {
            skipToPrevious()
        }
    }

    override fun onPlayNextSong() {
        MediaControllerCompat.getMediaController(this).transportControls.apply {
            skipToNext()
        }
    }

    override fun onSeekToPosition(position: Int) {
        MediaControllerCompat.getMediaController(this).transportControls.apply {
            seekTo(position.toLong())
        }
    }

    override fun onPlayerControlClicked(position: Int) {}

    override fun onShuffleModeEnable() {
        MediaControllerCompat.getMediaController(this).apply {
            transportControls.setShuffleMode(this.shuffleMode)
        }
        val controller = MediaControllerCompat.getMediaController(this)
        val playerControlFragment = supportFragmentManager.findFragmentByTag(PLAY_CONTROL_FRAGMENT)
            as PlayerControlFragment
        playerControlFragment.changeShuffleIconBackground(controller.shuffleMode)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        addFragmentInActivity()
    }

    private fun addFragmentInActivity() {
        val transition: FragmentTransaction = supportFragmentManager.beginTransaction()
        transition.replace(R.id.constraintLayout_main_mainContainer, discoverFragment)
        transition.replace(R.id.frameLayout_main_controlContainer, bottomFragment, PLAY_CONTROL_FRAGMENT)
        transition.addToBackStack("INITIAL_DISCOVER_FRAGMENT_AND_PLAYER_CONTROL_FRAGMENT")
        transition.commit()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, PlayService::class.java),
            connectionCallback,
            null
        )
        mediaBrowser.connect()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.home -> {
                drawerLayout_main.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        Timber.d("onBackPressed count: $count")
        val success = if (count == 1) {
            finish()
        } else {
            // get top of entry in the back stack
            val topBackStackEntry = supportFragmentManager.getBackStackEntryAt(count - 1)
            // pop the last fragment transaction
            supportFragmentManager.popBackStack(topBackStackEntry.name, POP_BACK_STACK_INCLUSIVE)
        }
        Timber.d( "onBackPressed success: $success")
    }

    fun showBottomPlayerControl() {
        val behavior = BottomSheetBehavior.from(frameLayout_main_controlContainer)
        val changeBounds = ChangeBounds()
        changeBounds.duration = 300
        TransitionManager.beginDelayedTransition(coordinatorLayout_main, changeBounds)
        behavior.peekHeight = 250
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowser.disconnect()
        currentPositionExecutorService.shutdown()
    }

    private fun schedulePositionUpdate() {
        if (!currentPositionExecutorService.isShutdown) {
            currentPositionExecutorService.scheduleAtFixedRate({
                currentPositionUpdateHandler.post {
                    bottomFragment.updateCurrentPosition(playbackState)
                }
            }, 100, 1000, TimeUnit.MILLISECONDS)
        }
    }

    inner class ControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Timber.d( "onPlaybackStateChanged stateBuilder: ${state.state}")
            bottomFragment.setPlayIcon(state.state)
            this@MainActivity.playbackState = state
            schedulePositionUpdate()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            bottomFragment.apply {
                metadata.getString("ALBUM_ARTWORK")?.let { setAlbumArtwork(it) }
                metadata.getString("ALBUM_ARTWORK")?.let { setBlurBackground(it) }
                metadata.getString("ARTIST_NAME")?.let { setArtistName(it) }
                metadata.getString("SONG_NAME")?.let { setSongName(it) }
                setDuration(metadata.getLong("DURATION").toInt())
            }
            this@MainActivity.playbackMetadata = metadata
        }
    }
}
