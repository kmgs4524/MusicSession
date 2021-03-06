package com.york.android.musicsession.view.artist

import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.squareup.picasso.Picasso
import com.york.android.musicsession.R
import com.york.android.musicsession.model.datafactory.GetArtistImage
import com.york.android.musicsession.model.bitmap.BlurBuilder
import com.york.android.musicsession.model.bitmap.GetBitmapFromUrl
import com.york.android.musicsession.model.datafactory.AlbumFactory
import com.york.android.musicsession.model.datafactory.SongFactory
import com.york.android.musicsession.model.data.Artist
import com.york.android.musicsession.view.exoplayer.SongAdapter
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.controlview.*
import kotlinx.android.synthetic.main.fragment_artist.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

class ArtistFragment : Fragment() {

    private var paramArtist: Artist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            paramArtist = arguments.getParcelable(ARTIST) as Artist
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_artist, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        // Inflate the layout for this fragment
        async(UI) {
            Log.d("ArtistFragment", "paramArtist.name: ${paramArtist!!.name}")
            val imageUrl = bg { GetArtistImage(activity).getImage(paramArtist!!.name) }
            Log.d("ArtistFragment", "imageUrl:${imageUrl.await()}")
            setArtistImage(imageUrl.await())
            setCollspseBackground(imageUrl.await())
            progressbar_artist.visibility = View.GONE
            coordinatorLayout_artist.visibility = View.VISIBLE
        }

        setArtistName()
        initArtistAlbumRecyclerView()
        initArtistSongRecyclerView()
    }

    fun setArtistName() {
        textView_artist_artistName.setText(paramArtist?.name)
        Log.d("ArtistFragment", "supportActionBar: ${(activity as AppCompatActivity).supportActionBar} title: ${paramArtist?.name}")
        appBarLayout_artist.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var scrollRange = -1
            var isShow = false

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                Log.d("onOffsetChanged", "verticalOffset: ${verticalOffset}")
                if (scrollRange == -1) {
                    Log.d("onOffsetChanged", "getTotalScrollRange: ${appBarLayout?.getTotalScrollRange()}")
                    scrollRange = appBarLayout?.getTotalScrollRange()!!
                }
                if (scrollRange + verticalOffset == 0) {
                    Log.d("onOffsetChanged", "is 0 ")
                    collapsingToolbarLayout_artist.isTitleEnabled = false
                    toolbar_artist.setTitle(paramArtist?.name)
                    isShow = true
                }
                else if(isShow) {
                    collapsingToolbarLayout_artist.setTitle(" ")    //carefull there should a space between double quote otherwise it wont work
                    isShow = false
                }
            }
        })
//        collapsingToolbarLayout_artist.isTitleEnabled = false
//        toolbar_artist.title = paramArtist?.name
    }

    private fun setArtistImage(imageUrl: String) {
        Picasso.get()
                .load(imageUrl)
                .into(circleImageView_artist_artistImage)
    }

    private fun setCollspseBackground(imageUrl: String) {
        async(UI) {
            Log.d("setCollspseBackground", "paramArtist.imageUrl: ${imageUrl}")
            val bitmap = bg { GetBitmapFromUrl().getBitmap(imageUrl) }
            val blurredBitmap = BlurBuilder().blur(bitmap.await()!!, activity)
            constraintLayout_artist_collapse.background = BitmapDrawable(resources, blurredBitmap)
            Log.d("ArtistFragment", "bitmap: ${bitmap}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initArtistAlbumRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        val albums = AlbumFactory(activity).getAlbums(paramArtist!!.name, "Artist")

        layoutManager.orientation = LinearLayout.HORIZONTAL
        recyclerView_artist_albums.layoutManager = layoutManager
        recyclerView_artist_albums.adapter = ArtistAlbumAdapter(albums, activity)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initArtistSongRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        val songs = SongFactory(activity).getSongs(paramArtist!!.name, "Artist")
        val handler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                val data = msg?.data
                Log.d("ArtistFragment", "data: ${data}")
                if (data != null) {
                    Log.d("ArtistFragment", "current position: ${data?.getInt("CURRENT_POSITION")} duration: ${data?.getInt("DURATION")}")
                    progressbar_album.progress = data?.getInt("CURRENT_POSITION")
                    progressbar_album.max = data?.getInt("DURATION")

                    textView_playerControl_artistNameTitle.setText(data?.getString("ARTIST"))
                    textView_controlView_songName.setText(data?.getString("SONG_NAME"))
                }
            }
        }

        recyclerView_artist_songs.layoutManager = layoutManager
        recyclerView_artist_songs.adapter = SongAdapter(songs, activity)
        recyclerView_artist_songs.addItemDecoration(DividerItemDecoration(activity, LinearLayout.VERTICAL))
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARTIST = "paramArtist"

        fun newInstance(param: Artist): ArtistFragment {
            val fragment = ArtistFragment()
            val args = Bundle()
            args.putParcelable(ARTIST, param)
            fragment.arguments = args
            return fragment
        }
    }
}
