package com.york.android.musicsession.view.album

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Album
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.view.exoplayer.SongAdapter
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.controlview.*
import kotlinx.android.synthetic.main.fragment_album.*
import java.util.ArrayList

class AlbumFragment : Fragment(), AlbumView {

    // TODO: Rename and change types of parameters
    private var album: Album? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            album = arguments.getParcelable(ARG_PARAM_ALBUM)
            album.let {
                Log.d("AlbumFragment", "album name: ${(album as Album).name} artist: ${(album as Album).artist}")
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_album, container, false)
    }

    override fun onStart() {
        super.onStart()
        album?.let {
            setAlbumNameArtist(it.name, it.artist)
            setAlbumArt(it.coverImageUrl)
            initRecyclerView(it.songs)
        }
    }

    fun setAlbumNameArtist(albumName: String, artist: String) {
        textView_album_albumName.setText(albumName)
        textView_album_artist.setText(artist)
    }

    fun setAlbumArt(albumArtPath: String) {
        imageView_album_artwork.setImageBitmap(BitmapFactory.decodeFile(albumArtPath))
    }

    fun initRecyclerView(songs: ArrayList<Song>) {
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
        recyclerView_album.layoutManager = LinearLayoutManager(activity)
        recyclerView_album.adapter = SongAdapter(songs, activity, null, null, handler)
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
        private val ARG_PARAM_ALBUM = "Album"

        // TODO: Rename and change types and number of parameters
        fun newInstance(param: Album): AlbumFragment {
            val fragment = AlbumFragment()
            val args = Bundle()

            args.putParcelable(ARG_PARAM_ALBUM, param)
            fragment.arguments = args

            return fragment
        }
    }
}
