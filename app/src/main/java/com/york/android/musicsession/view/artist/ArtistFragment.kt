package com.york.android.musicsession.view.artist

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.squareup.picasso.Picasso

import com.york.android.musicsession.R
import com.york.android.musicsession.model.GetArtistImage
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

    private var param: Artist? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            param = arguments.getParcelable(ARG_PARAM_ARTIST) as Artist
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
            Log.d("ArtistFragment", "param.name: ${param!!.name}")
            val imageUrl = bg { GetArtistImage(activity).getImage(param!!.name) }
            Log.d("ArtistFragment", "imageUrl:${imageUrl.await()}")
            setArtistImage(imageUrl.await())
            setCollspseBackground(imageUrl.await())
            progressbar_artist.visibility = View.GONE
            coordinatorLayout_artist.visibility = View.VISIBLE
        }
//        val imageUrl = "https://i.kfs.io/artist/global/1210236,0v8/fit/160x160.jpg"
//        setArtistImage(imageUrl)

        setArtistName()
        initArtistAlbumRecyclerView()
        initArtistSongRecyclerView()
    }

    fun setArtistName() {
        textView_artist_artistName.setText(param?.name)
    }

    fun setArtistImage(imageUrl: String) {
        Picasso.get()
                .load(imageUrl)
                .into(circleImageView_artist_artistImage)
    }

    fun setCollspseBackground(imageUrl: String) {
        async(UI) {
            Log.d("setCollspseBackground", "param.imageUrl: ${imageUrl}")
            val bitmap = bg { GetBitmapFromUrl().getBitmap(imageUrl) }
            val blurredBitmap = BlurBuilder().blur(bitmap.await()!!, activity)
            constraintLayout_artist_collapse.background = BitmapDrawable(resources, blurredBitmap)
            Log.d("ArtistFragment", "bitmap: ${bitmap}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initArtistAlbumRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        val albums = AlbumFactory(activity).getAlbums(param!!.name, "Artist")

        layoutManager.orientation = LinearLayout.HORIZONTAL
        recyclerView_artist_albums.layoutManager = layoutManager
        recyclerView_artist_albums.adapter = ArtistAlbumAdapter(albums, activity)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initArtistSongRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        val songs = SongFactory(activity).getSongs(param!!.name, "Artist")
        val handler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                val data = msg?.data
//                Log.d("thread check", "current thread id: ${Thread.currentThread().id}")
                Log.d("handler", "data: ${data}")
                if (data != null) {
                    Log.d("handler", "current position: ${data?.getInt("CURRENT_POSITION")} duration: ${data?.getInt("DURATION")}")
                    progressbar_album.progress = data?.getInt("CURRENT_POSITION")
                    progressbar_album.max = data?.getInt("DURATION")

                    textView_controlview_artist.setText(data?.getString("ARTIST"))
                    textView_controlView_songName.setText(data?.getString("SONG_NAME"))
                }
            }
        }

        recyclerView_artist_songs.layoutManager = layoutManager
        recyclerView_artist_songs.adapter = SongAdapter(songs, activity)
        recyclerView_artist_songs.addItemDecoration(DividerItemDecoration(activity, LinearLayout.VERTICAL))
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM_ARTIST = "paramArtist"

        // TODO: Rename and change types and number of parameters
        fun newInstance(param: Artist): ArtistFragment {
            val fragment = ArtistFragment()
            val args = Bundle()
            args.putParcelable(ARG_PARAM_ARTIST, param)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
