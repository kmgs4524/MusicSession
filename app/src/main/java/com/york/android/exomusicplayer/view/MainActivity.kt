package com.york.android.exomusicplayer.view

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

import com.york.android.exomusicplayer.service.PlayService
import com.york.android.exomusicplayer.R
import com.york.android.exomusicplayer.model.Song

class MainActivity : AppCompatActivity() {
    val filePath = "/storage/emulated/0/Music/吳汶芳 - 我來自"
    // verify permission
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecycleView()
    }

    fun initRecycleView() {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val songs = ArrayList<Song>()

        songs.add(Song("還島快樂", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳,舒米恩-還島快樂.mp3"))
        songs.add(Song("不要來找我 (成全遼闊版)", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-不要來找我 (成全遼闊版).mp3"))
        songs.add(Song("不要來找我 (放手解脫版)", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-不要來找我 (放手解脫版).mp3"))
        songs.add(Song("心之所向", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-心之所向.mp3"))
        songs.add(Song("水", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-水.mp3"))
        songs.add(Song("我來自", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-我來自.mp3"))
        songs.add(Song("指南", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-指南.mp3"))
        songs.add(Song("美好", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-美好.mp3"))
        songs.add(Song("迷路汪洋", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-迷路汪洋.mp3"))
        songs.add(Song("散落的星空", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-散落的星空.mp3"))
        songs.add(Song("無窮", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-無窮.mp3"))
        songs.add(Song("總有一天", "吳汶芳", R.drawable.album_cover, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-總有一天.mp3"))

        recyclerView_album.layoutManager = LinearLayoutManager(this)
        recyclerView_album.adapter = SongAdapter(songs, this, null)
    }


//    fun setService() {
//        val intent = Intent()
//
//        // get permission for reading file
//        verifyStoragePermission()
//
//        // start and bind service
//        intent.setClass(this, PlayService::class.java)
//        startService(intent)
//        bindService(intent, MusicServiceConnection(), 0)
//    }

    fun verifyStoragePermission() {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

//    inner class MusicServiceConnection: ServiceConnection {
//        override fun onServiceDisconnected(p0: ComponentName?) {
//
//        }
//
//        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
//            val service = (binder as PlayService.LocalBinder).getService()
//            playerView_main.player = service.player
//        }
//    }

}
