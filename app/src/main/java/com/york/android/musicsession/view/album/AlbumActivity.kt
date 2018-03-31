package com.york.android.musicsession.view.album

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_album.*

import com.york.android.musicsession.R
import com.york.android.musicsession.model.Song
import kotlinx.android.synthetic.main.controlview.*

class AlbumActivity : AppCompatActivity() {
    val filePath = "/storage/emulated/0/Music/吳汶芳 - 我來自"
    // verify permission
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    var songAdapter: SongAdapter? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

    }



    fun initRecycleView(handler: Handler) {
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


        Log.d("initRecycleView", "called")
        recyclerView_album.layoutManager = LinearLayoutManager(this)
//        songAdapter = SongAdapter(songs, this, null, null, handler)
        recyclerView_album.adapter = SongAdapter(songs, this, null, null, handler)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStart() {
        super.onStart()

        // UI Handler
        val handler = object: Handler() {
            override fun handleMessage(msg: Message?) {
                val data = msg?.data
//                Log.d("thread check", "current thread id: ${Thread.currentThread().id}")
                Log.d("handler", "data: ${data}")
                if(data != null) {
                    Log.d("handler", "current position: ${data?.getInt("CURRENT_POSITION")} duration: ${data?.getInt("DURATION")}")
                    progressbar_album.progress = data?.getInt("CURRENT_POSITION")
                    progressbar_album.max = data?.getInt("DURATION")

                    textView_controlview_artist.setText(data?.getString("ARTIST"))
                    textView_controlview_songname.setText(data?.getString("SONG_NAME"))
                }
            }
        }

        initRecycleView(handler)
    }

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
        songAdapter?.unbindService()
    }
}
