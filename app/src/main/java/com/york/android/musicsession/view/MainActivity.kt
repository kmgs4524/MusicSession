package com.york.android.musicsession.view

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.util.Log
import android.view.*
import com.york.android.musicsession.R
import com.york.android.musicsession.model.bitmap.BlurBuilder
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.model.datafactory.SongFactory
import com.york.android.musicsession.service.PlayService
import com.york.android.musicsession.view.album.AlbumFragment
import com.york.android.musicsession.view.mymusic.MyMusicFragment
import com.york.android.musicsession.view.playercontrol.PlayerControlDialogFragment
import com.york.android.musicsession.view.playercontrol.PlayerControlFragment
import com.york.android.musicsession.view.albumpage.AlbumPageFragment
import com.york.android.musicsession.view.artist.ArtistFragment
import com.york.android.musicsession.view.artistpage.ArtistPageFragment
import com.york.android.musicsession.view.playlist.PlaylistPageFragment
import com.york.android.musicsession.view.songpage.SongPageFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controlview.*

class MainActivity : AppCompatActivity(), PlayerControlFragment.OnFragmentInteractionListener, LibraryFragment.OnFragmentInteractionListener,
        SongPageFragment.OnFragmentInteractionListener, AlbumPageFragment.OnFragmentInteractionListener,
        ArtistPageFragment.OnFragmentInteractionListener, MyMusicFragment.OnFragmentInteractionListener,
        PlayerControlDialogFragment.Listener, AlbumFragment.OnFragmentInteractionListener,
        ArtistFragment.OnFragmentInteractionListener, PlaylistPageFragment.OnFragmentInteractionListener {

    lateinit var service: PlayService
    lateinit var timeHandler: Handler
    lateinit var infoHandler: Handler
    lateinit var statusHandler: Handler

    fun bindPlayService(songs: List<Song>) {
//        val songs = ArrayList<Song>()   // put to PlayService
        val intent = Intent()
        val connection = MusicServiceConnection(songs, timeHandler, infoHandler, statusHandler)
        intent.setClass(this, PlayService::class.java)
        startService(intent)
        bindService(intent, connection, 0)
    }

    fun playMedia(index: Int) {
        service.playMediaSource(index)
    }

    override fun onDisplaySong() {
        service.display()
    }

    override fun onPauseSong() {
        service.pause()
    }

    override fun onPlayPrevSong() {
        service.playPrevious()
    }

    override fun onPlayNextSong() {
        service.playNext()
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

        val transition:FragmentTransaction = supportFragmentManager.beginTransaction()

        transition.add(R.id.constraintLayout_main_mainContainer, discoverFragment)
        transition.add(R.id.frameLayout_main_controlContainer, bottomFragment)
        transition.addToBackStack(null)
        transition.commit()

        setDrawerListener()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.home -> {
                drawerLayout_main.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setDrawerListener() {
        navigationDrawer_main.setNavigationItemSelectedListener({item: MenuItem ->
            val transition:FragmentTransaction = supportFragmentManager.beginTransaction()

            when(item.itemId) {
                R.id.nav_mymusic ->   transition.replace(R.id.constraintLayout_main_mainContainer, myMusicFragment)
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
//        setBlurBackground()
        // bottom sheet fragment
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setBlurBackground() {
        val songs = SongFactory(this).getSongs("C.NARI", "Artist")
        if(songs[0].coverImageUrl != "none") {
            Log.d("PlayerControlFragment", "coverImageUrl: ${songs[0].coverImageUrl}")
            val coverBitmap = BitmapFactory.decodeFile(songs[0].coverImageUrl)
            Log.d("PlayerControlFragment", "coverBitmap: ${coverBitmap}")
            val blurredBitmap = BlurBuilder().blur(coverBitmap, this)
            Log.d("PlayerControlFragment", "blurredBitmap: ${blurredBitmap}")
            constraintLayout_controlView.background = BitmapDrawable(resources, blurredBitmap)
            imageView_controlView_artwork.setImageBitmap(coverBitmap)
        }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        Log.d("MainActivity", "onBackPressed count: ${count}")
        val success = if(count == 0)  {
            super.onBackPressed()
        } else {
            // get top of entry in the back stack
            val topBackStackEntry = supportFragmentManager.getBackStackEntryAt(count - 1)
            // pop the last fragment transaction
            supportFragmentManager.popBackStack(topBackStackEntry.name, POP_BACK_STACK_INCLUSIVE)
        }
        Log.d("MainActivity", "onBackPressed success: ${success}")
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class MusicServiceConnection(var songs: List<Song>, val timeHandler: Handler, val infoHandler: Handler,
                                       val statusHandler: Handler): ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            Log.d("onServiceConnected", "p0: ${p0}, binder: ${binder}")
            service = (binder as PlayService.LocalBinder).getService()
            Log.d("onServiceConnected", "service: ${service}")
            service.timeHandler = timeHandler
            service.infoHandler = infoHandler
            service.statusHandler = statusHandler
            service.createConcatenatingMediaSource(songs)
        }

    }
}
