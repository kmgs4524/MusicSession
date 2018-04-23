package com.york.android.musicsession.view

import android.content.*
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomSheetBehavior
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.support.v4.app.FragmentTransaction
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.view.GravityCompat
import android.util.Log
import android.view.*
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.service.PlayService
import com.york.android.musicsession.view.album.AlbumFragment
import com.york.android.musicsession.view.mymusic.MyMusicFragment
import com.york.android.musicsession.view.playercontrol.PlayerControlDialogFragment
import com.york.android.musicsession.view.playercontrol.PlayerControlFragment
import com.york.android.musicsession.view.albumpage.AlbumPageFragment
import com.york.android.musicsession.view.artist.ArtistFragment
import com.york.android.musicsession.view.artistpage.ArtistPageFragment
import com.york.android.musicsession.view.notification.PlayerNotificationBuilder
import com.york.android.musicsession.view.playlist.PlaylistPageFragment
import com.york.android.musicsession.view.songpage.SongPageFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), PlayerControlFragment.OnFragmentInteractionListener, LibraryFragment.OnFragmentInteractionListener,
        SongPageFragment.OnFragmentInteractionListener, AlbumPageFragment.OnFragmentInteractionListener,
        ArtistPageFragment.OnFragmentInteractionListener, MyMusicFragment.OnFragmentInteractionListener,
        PlayerControlDialogFragment.Listener, AlbumFragment.OnFragmentInteractionListener,
        ArtistFragment.OnFragmentInteractionListener, PlaylistPageFragment.OnFragmentInteractionListener {

    // service used to play music
    var service: PlayService? = null
    // update playback's stateBuilder
    lateinit var timeHandler: Handler
    lateinit var infoHandler: Handler
    lateinit var statusHandler: Handler
    // MusicSession framework component
    val connectionCallback = ConnectionCallback()
    val subscriptionCallback = SubscriptionCallback()
    val controllerCallback = ControllerCallback()
    lateinit var mediaBrowser: MediaBrowserCompat   // Browses media content offered by a MediaBrowserServiceCompat.
    lateinit var sessionToken: MediaSessionCompat.Token
    lateinit var controller: MediaControllerCompat
    lateinit var transportControls: MediaControllerCompat.TransportControls
    // used to bind PlayService
    lateinit var connection: MusicServiceConnection

    var songs: List<Song> = ArrayList<Song>()
    var currentWindowIndex = 0
    lateinit var songUri: Uri

    lateinit var playbackState: PlaybackStateCompat

    fun bindPlayService(songs: List<Song>) {
        if (service == null) {
            Log.d("MainActivity", "service is null")
            val intent = Intent()
            intent.setClass(this, PlayService::class.java)
            startService(intent)
            var connection = MusicServiceConnection(songs, timeHandler, infoHandler, statusHandler)
            bindService(intent, connection, 0)
        }
    }

    fun setNotification(song: Song) {
        val notification = PlayerNotificationBuilder(applicationContext, song)
        notification.show()
    }

    // used to get the MediaController
    inner class ConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.d("playSong", "onConnected")
            sessionToken = mediaBrowser.sessionToken
            // MediaController give access to everything.
            controller = MediaControllerCompat(this@MainActivity, sessionToken)
            // MediaController registers callback to change UI when playback's stateBuilder changes.
            controller.registerCallback(controllerCallback)
            // set created MediaController in order to use it everywhere
            MediaControllerCompat.setMediaController(this@MainActivity, controller)
            transportControls = controller.transportControls

        }

        override fun onConnectionSuspended() {

            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    fun setPlaylist(songs: List<Song>, index: Int) {
        this.songs = songs
        this.currentWindowIndex = index
        songUri = Uri.parse(songs[index].filePath)
        Log.d("MainActivity", "setPlaylist songs ${songs} service: ${service}")
        // old version
//        service?.createMediaSource(songs)

        // new version
        // MediaBrowserCompat wraps the API for bound services
        val bundle = Bundle()
        bundle.putParcelableArrayList("SONGS", songs as ArrayList)
        bundle.putInt("CURRENT_WINDOW_INDEX", index)
        transportControls.playFromUri(songUri, bundle)
        Log.d("playSong", "uri: ${songUri} hashcode: ${bundle.hashCode()} bundle: ${bundle} ")
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
        service?.playPrevious()
    }

    override fun onPlayNextSong() {
        Thread(Runnable {
            service?.playNext()
        }).run()

    }

    override fun onSeekToPosition(position: Int) {

    }

    override fun onPlayerControlClicked(position: Int) {

    }

    val bottomFragment = PlayerControlFragment.newInstance("", "")
    val myMusicFragment = MyMusicFragment.newInstance("", "")
    val playlisPageFragment = PlaylistPageFragment.newInstance("", "")
    val discoverFragment = LibraryFragment.newInstance("", "")

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setDrawerListener()
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

    fun setDrawerListener() {
        navigationDrawer_main.setNavigationItemSelectedListener({ item: MenuItem ->
            val transition: FragmentTransaction = supportFragmentManager.beginTransaction()

            when (item.itemId) {
                R.id.nav_mymusic -> transition.replace(R.id.constraintLayout_main_mainContainer, myMusicFragment)
                R.id.nav_playlist -> transition.replace(R.id.constraintLayout_main_mainContainer, playlisPageFragment)
                R.id.nav_dicover -> transition.replace(R.id.constraintLayout_main_mainContainer, discoverFragment)
                else -> true
            }
            transition.addToBackStack(null)
            transition.commit()

            drawerLayout_main.closeDrawers()
            true
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        // add content and bottom fragments
        val transition: FragmentTransaction = supportFragmentManager.beginTransaction()

        transition.add(R.id.constraintLayout_main_mainContainer, discoverFragment)
        transition.add(R.id.frameLayout_main_controlContainer, bottomFragment, "PLAYER_CONTROL_FRAGMENT")
        transition.addToBackStack(null)
        transition.commit()

        mediaBrowser = MediaBrowserCompat(this, ComponentName(this, PlayService::class.java), connectionCallback, null)
        mediaBrowser.connect()
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        Log.d("MainActivity", "onBackPressed count: ${count}")
        val success = if (count == 0) {
            super.onBackPressed()
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
//        val fade = TransitionInflater.from(this).inflateTransition(R.transition.fade)
        val changeBounds = ChangeBounds()
        changeBounds.duration = 300
        TransitionManager.beginDelayedTransition(coordinatorLayout_main, changeBounds)
        behavior.peekHeight = 250
    }

    override fun onDestroy() {
        super.onDestroy()
//        unbindService(connection)
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    val currentPositionExecutorService = Executors.newSingleThreadScheduledExecutor()
    val currentPositionUpdateHandler = Handler()
    val updateTask = Runnable {
        val playerControlFragment = supportFragmentManager.findFragmentByTag("PLAYER_CONTROL_FRAGMENT") as PlayerControlFragment
        playerControlFragment.updateCurrentPosition(playbackState)
    }

    fun schedulePositionUpdate() {
        if(!currentPositionExecutorService.isShutdown) {
            currentPositionExecutorService.scheduleAtFixedRate(Runnable {
                currentPositionUpdateHandler.post(updateTask)
            }, 100, 1000, TimeUnit.MILLISECONDS)
        }
    }

    inner class MusicServiceConnection(var songs: List<Song>, val timeHandler: Handler, val infoHandler: Handler,
                                       val statusHandler: Handler) : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            Log.d("onServiceConnected", "p0: ${p0}, binder: ${binder}")
            service = (binder as PlayService.LocalBinder).getService()
            Log.d("onServiceConnected", "service: ${service}")
//            service?.timeHandler = timeHandler
//            service?.infoHandler = infoHandler
//            service?.statusHandler = statusHandler

//            service?.createMediaSource(songs)
        }

    }

    inner class ControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            // When playback stateBuilder changed, onPlaybackStateChanged shold be called.
            val playerControlFragment = supportFragmentManager.findFragmentByTag("PLAYER_CONTROL_FRAGMENT") as PlayerControlFragment
            Log.d("MainActivity", "onPlaybackStateChanged stateBuilder: ${state?.state!!}")
            playerControlFragment.setPlayIcon(state?.state!!)

            playerControlFragment.updateCurrentPosition(state)

            this@MainActivity.playbackState = state
            schedulePositionUpdate()
//            playerControlFragment.setCurrentPostition(state?.position.toInt())
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            val playerControlFragment = supportFragmentManager.findFragmentByTag("PLAYER_CONTROL_FRAGMENT") as PlayerControlFragment
            playerControlFragment.setAlbumArtwork(metadata?.getString("ALBUM_ARTWORK")!!)
            playerControlFragment.setBlurBackground(metadata?.getString("ALBUM_ARTWORK")!!)
            playerControlFragment.setArtistName(metadata?.getString("ARTIST_NAME")!!)
            playerControlFragment.setSongName(metadata?.getString("SONG_NAME")!!)
            playerControlFragment.setDuration(metadata?.getLong("DURATION").toInt())
        }
    }

    // show the content from service
    inner class SubscriptionCallback : MediaBrowser.SubscriptionCallback() {}
}
