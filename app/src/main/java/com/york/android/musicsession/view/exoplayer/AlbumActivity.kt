package com.york.android.musicsession.view.exoplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_album.*

import com.york.android.musicsession.R
import com.york.android.musicsession.model.bitmap.BlurBuilder
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.model.datafactory.SongFactory
import kotlinx.android.synthetic.main.controlview.*


class AlbumActivity : AppCompatActivity() {
    val filePath = "/storage/emulated/0/Music/吳汶芳 - 我來自"
    // verify permission
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    var songAdapter: SongAdapter? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        setBlurBackground()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setBlurBackground() {

            val songs = SongFactory(this@AlbumActivity).getSongs("C.NARI", "Artist")
            if(songs[0].coverImageUrl != "none") {
                Log.d("PlayerControlFragment", "coverImageUrl: ${songs[0].coverImageUrl}")
                val coverBitmap = BitmapFactory.decodeFile(songs[0].coverImageUrl)
                Log.d("PlayerControlFragment", "coverBitmap: ${coverBitmap}")
                val blurredBitmap = BlurBuilder().blur(coverBitmap, this@AlbumActivity)
                Log.d("PlayerControlFragment", "blurredBitmap: ${blurredBitmap}")
                constraintLayout_controlView.background = BitmapDrawable(resources, blurredBitmap)
                imageView_controlView_artwork.setImageBitmap(coverBitmap)
            }

    }

    fun initRecycleView(handler: Handler) {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val songs = ArrayList<Song>()

        songs.add(Song("還島快樂", "吳汶芳", "", "", 0, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳,舒米恩-還島快樂.mp3"))
        songs.add(Song("不要來找我 (成全遼闊版)", "吳汶芳", "", "", 0, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-不要來找我 (成全遼闊版).mp3"))
        songs.add(Song("不要來找我 (放手解脫版)", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-不要來找我 (放手解脫版).mp3"))
        songs.add(Song("心之所向", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-心之所向.mp3"))
        songs.add(Song("水", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-水.mp3"))
        songs.add(Song("我來自", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-我來自.mp3"))
        songs.add(Song("指南", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-指南.mp3"))
        songs.add(Song("美好", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-美好.mp3"))
        songs.add(Song("迷路汪洋", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-迷路汪洋.mp3"))
        songs.add(Song("散落的星空", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-散落的星空.mp3"))
        songs.add(Song("無窮", "吳汶芳", "", "", 0,"/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-無窮.mp3"))
        songs.add(Song("總有一天", "吳汶芳", "", "", 0, "/storage/emulated/0/Music/吳汶芳 - 我來自/吳汶芳-總有一天.mp3"))


        Log.d("initRecycleView", "called")
        recyclerView_album_activity.layoutManager = LinearLayoutManager(this)
//        songAdapter = SongAdapter(songs, this, null, null, handler)
        recyclerView_album_activity.adapter = SongAdapter(songs, this, null, null, handler)
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
                    textView_controlView_songName.setText(data?.getString("SONG_NAME"))
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
//        songAdapter?.unbindService()
    }
}
