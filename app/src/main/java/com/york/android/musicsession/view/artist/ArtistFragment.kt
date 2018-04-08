package com.york.android.musicsession.view.artist

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.york.android.musicsession.R
import com.york.android.musicsession.model.AlbumFactory
import com.york.android.musicsession.model.SongFactory
import com.york.android.musicsession.model.data.Album
import com.york.android.musicsession.view.albumpage.AlbumAdapter
import com.york.android.musicsession.view.exoplayer.SongAdapter
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.controlview.*
import kotlinx.android.synthetic.main.fragment_artist.*

class ArtistFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_artist, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        initArtistAlbumRecyclerView()
        initArtistSongRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initArtistAlbumRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        val albums = AlbumFactory(activity).getAlbums("吳汶芳", "Artist")

        layoutManager.orientation = LinearLayout.HORIZONTAL
        recyclerView_artist_albums.layoutManager = layoutManager
        recyclerView_artist_albums.adapter = ArtistAlbumAdapter(albums, activity)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initArtistSongRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        val songs = SongFactory(activity).getSongs("吳汶芳", "Artist")
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

        recyclerView_artist_songs.layoutManager = layoutManager
        recyclerView_artist_songs.adapter = SongAdapter(songs, activity, null, null, handler)
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
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): ArtistFragment {
            val fragment = ArtistFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor