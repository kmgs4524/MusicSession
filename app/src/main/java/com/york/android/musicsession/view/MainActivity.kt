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
import android.util.Log
import android.view.*
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.service.PlayService
import com.york.android.musicsession.view.playercontrol.PlayerControlDialogFragment
import com.york.android.musicsession.view.playercontrol.PlayerControlFragment
import com.york.android.musicsession.view.playercontrol.PlayerControlFragment.Companion.PLAY_CONTROL_FRAGMENT
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), PlayerControlFragment.OnFragmentInteractionListener,
        PlayerControlDialogFragment.Listener {

    private var service: PlayService? = null

    // MediaSession framework component
    private val connectionCallback = ConnectionCallback()
    val controllerCallback = ControllerCallback()
    lateinit var mediaBrowser: MediaBrowserCompat   // Browses media content offered by a MediaBrowserServiceCompat.
    lateinit var controller: MediaControllerCompat
    lateinit var transportControls: MediaControllerCompat.TransportControls

    var songs: List<Song> = ArrayList()
    var currentWindowIndex = 0
    lateinit var songUri: Uri

    lateinit var playbackState: PlaybackStateCompat
    lateinit var playbackMetadata: MediaMetadataCompat

    // updating current position in specified interval needs ExecutorService, Handler
    private val currentPositionExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val currentPositionUpdateHandler = Handler()
    private val updateTask = Runnable {
        val playerControlFragment = supportFragmentManager.findFragmentByTag(PLAY_CONTROL_FRAGMENT) as? PlayerControlFragment
        playerControlFragment?.updateCurrentPosition(playbackState)
    }

    // used to get the MediaController
    inner class ConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // MediaController give access to everything.
            controller = MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
            // MediaController registers mediaSessionCallback to change UI when playback's stateBuilder changes.
            controller.registerCallback(controllerCallback)
            // set created MediaController in order to use it everywhere
            MediaControllerCompat.setMediaController(this@MainActivity, controller)
            transportControls = controller.transportControls
        }
    }

    fun setPlaylist(songs: List<Song>, index: Int) {
        this.songs = songs
        this.currentWindowIndex = index
        songUri = Uri.parse(songs[index].filePath)
        Log.d("MainActivity", "setPlaylist songs $songs service: $service")

        // new version
        // MediaBrowserCompat wraps the API for bound services
        val bundle = Bundle()
        bundle.putParcelableArrayList("SONGS", songs as ArrayList)
        bundle.putInt("CURRENT_WINDOW_INDEX", index)
        transportControls.playFromUri(songUri, bundle)
        Log.d("playSong", "uri: ${songUri} hashcode: ${bundle.hashCode()} bundle: $bundle ")
    }

    fun playMedia(index: Int) {
        service?.playMediaSource(index)
    }

    override fun onDisplaySong() {
        transportControls.play()
    }

    override fun onPauseSong() {
        transportControls.pause()
    }

    override fun onPlayPrevSong() {
        transportControls.skipToPrevious()
    }

    override fun onPlayNextSong() {
        transportControls.skipToNext()
    }

    override fun onSeekToPosition(position: Int) {
        transportControls.seekTo(position.toLong())
    }

    override fun onPlayerControlClicked(position: Int) {

    }

    override fun onShuffleModeEnable() {
        transportControls.setShuffleMode(controller.shuffleMode)
        val playerControlFragment = supportFragmentManager.findFragmentByTag(PLAY_CONTROL_FRAGMENT) as PlayerControlFragment
        Log.d("MainActivity", "shuffleMode: ${controller.shuffleMode}")
        playerControlFragment.changeShuffleIconBackground(controller.shuffleMode)
    }

    private val bottomFragment = PlayerControlFragment.newInstance()
    private val discoverFragment = LibraryFragment.newInstance()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        addFragmentInActivity()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        mediaBrowser = MediaBrowserCompat(this, ComponentName(this, PlayService::class.java), connectionCallback, null)
        mediaBrowser.connect()
    }

    private fun addFragmentInActivity() {
        val transition: FragmentTransaction = supportFragmentManager.beginTransaction()
        transition.replace(R.id.constraintLayout_main_mainContainer, discoverFragment)
        transition.replace(R.id.frameLayout_main_controlContainer, bottomFragment, PLAY_CONTROL_FRAGMENT)
        transition.addToBackStack("INITIAL_DISCOVER_FRAGMENT_AND_PLAYER_CONTROL_FRAGMENT")
        transition.commit()
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount


        Log.d("MainActivity", "onBackPressed count: ${count}")
        val success = if (count == 1) {
            finish()
        } else {
            // get top of entry in the back stack
            val topBackStackEntry = supportFragmentManager.getBackStackEntryAt(count - 1)
            // pop the last fragment transaction
            supportFragmentManager.popBackStack(topBackStackEntry.name, POP_BACK_STACK_INCLUSIVE)
        }
        Log.d("MainActivity", "onBackPressed success: ${success}")
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
    }

    fun schedulePositionUpdate() {
        if (!currentPositionExecutorService.isShutdown) {
            currentPositionExecutorService.scheduleAtFixedRate({
                currentPositionUpdateHandler.post(updateTask)
            }, 100, 1000, TimeUnit.MILLISECONDS)
        }
    }

    inner class ControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            // When playback stateBuilder changed, onPlaybackStateChanged shold be called.
            val playerControlFragment = supportFragmentManager.findFragmentByTag(PLAY_CONTROL_FRAGMENT) as PlayerControlFragment
            Log.d("MainActivity", "onPlaybackStateChanged stateBuilder: ${state?.state!!}")
            playerControlFragment.setPlayIcon(state?.state!!)
            this@MainActivity.playbackState = state
            schedulePositionUpdate()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            val playerControlFragment = supportFragmentManager.findFragmentByTag(PLAY_CONTROL_FRAGMENT) as PlayerControlFragment
            playerControlFragment.setAlbumArtwork(metadata?.getString("ALBUM_ARTWORK")!!)
            playerControlFragment.setBlurBackground(metadata.getString("ALBUM_ARTWORK")!!)
            playerControlFragment.setArtistName(metadata.getString("ARTIST_NAME")!!)
            playerControlFragment.setSongName(metadata.getString("SONG_NAME")!!)
            playerControlFragment.setDuration(metadata.getLong("DURATION").toInt())
            this@MainActivity.playbackMetadata = metadata
        }
    }
}
