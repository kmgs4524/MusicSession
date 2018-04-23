package com.york.android.musicsession.view.playercontrol

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.york.android.musicsession.R
import com.york.android.musicsession.model.bitmap.BitmapCompression
import com.york.android.musicsession.model.bitmap.BlurBuilder
import com.york.android.musicsession.model.datafactory.SongFactory
import com.york.android.musicsession.view.MainActivity
import kotlinx.android.synthetic.main.fragment_player_control.*


class PlayerControlFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    val timeHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            val duration = msg?.data?.getInt("DURATION")!!
            val currentPosition = msg?.data?.getInt("CURRENT_POSITION")!!

            setSeekBar(duration, currentPosition)
            setTextViewDuration(duration)
            setTextViewCurrentPostition(currentPosition)
        }
    }

    // responsible for showing display button and pause button
    val statusHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            Log.d("infoHandler", "${msg?.data?.getBoolean("IS_PLAYING")!!}")
            if (msg?.data?.getBoolean("IS_PLAYING")!!) {
                imageView_playerControl_pause.visibility = View.VISIBLE
                imageView_playerControl_play.visibility = View.GONE
            } else {
                imageView_playerControl_play.visibility = View.VISIBLE
                imageView_playerControl_pause.visibility = View.GONE
            }
        }
    }

    val infoHandler = object : Handler() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun handleMessage(msg: Message?) {
            setSongName(msg?.data?.getString("SONG_NAME")!!)
            setArtistName(msg?.data?.getString("ARTIST_NAME")!!)
            setBlurBackground(msg?.data?.getString("ALBUM_ARTWORK")!!)
            setAlbumArtwork(msg?.data?.getString("ALBUM_ARTWORK")!!)

            Log.d("infoHandler", "${msg?.data?.getBoolean("IS_PLAYING")!!}")
            if (msg?.data?.getBoolean("IS_PLAYING")!!) {
                imageView_playerControl_pause.visibility = View.VISIBLE
                imageView_playerControl_play.visibility = View.GONE
            } else {
                imageView_playerControl_play.visibility = View.VISIBLE
                imageView_playerControl_pause.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_player_control, container, false)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        (activity as MainActivity).timeHandler = timeHandler
        (activity as MainActivity).infoHandler = infoHandler
        (activity as MainActivity).statusHandler = statusHandler

        imageView_playerControl_pause.setOnClickListener {
            (activity as MainActivity).onPauseSong()
        }

        imageView_playerControl_play.setOnClickListener {
            (activity as MainActivity).onDisplaySong()
        }

        imageView_playerControl_prev.setOnClickListener {
            (activity as MainActivity).onPlayPrevSong()
        }

        imageView_playerControl_next.setOnClickListener {
            (activity as MainActivity).onPlayNextSong()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setBlurBackground(imageUrl: String) {
        val songs = SongFactory(activity).getSongs("吳汶芳", "Artist")
        if (imageUrl != "none") {
            Log.d("PlayerControlFragment", "coverImageUrl: ${imageUrl}")
            val coverBitmap = BitmapFactory.decodeFile(imageUrl)
            Log.d("PlayerControlFragment", "coverBitmap: ${coverBitmap}")
            val blurredBitmap = BlurBuilder().blur(coverBitmap, activity)
            Log.d("PlayerControlFragment", "blurredBitmap: ${blurredBitmap}")
            constrainLayout_playerControl_container.background = BitmapDrawable(resources, blurredBitmap)
//            constraintLayout_controlView.background = BitmapDrawable(resources, blurredBitmap)
            imageView_playerControl_artwork.setImageBitmap(coverBitmap)
        }
    }

    fun setPlayIcon(state: Int) {
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            imageView_playerControl_pause.visibility = View.VISIBLE
            imageView_playerControl_play.visibility = View.GONE
        } else {
            imageView_playerControl_play.visibility = View.VISIBLE
            imageView_playerControl_pause.visibility = View.GONE
        }
    }

    fun setAlbumArtwork(imageUrl: String) {
        val bitmap = BitmapCompression.compressBySize(imageUrl, 200, 200)
        imageView_playerControl_artwork.setImageBitmap(bitmap)
        imageView_playerControl_artworkSmall.setImageBitmap(bitmap)
    }

    fun setArtistName(artistName: String) {
        textView_playerControl_artistName.setText(artistName)
        textView_playerControl_artistNameTitle.setText(artistName)
    }

    fun setSongName(songName: String) {
        textView_playerControl_songName.setText(songName)
        textView_playerControl_songNameTitle.setText(songName)
    }

    fun setSeekBar(duration: Int, currentPosition: Int) {
        seekBar_playerControl.max = duration
        seekBar_playerControl.progress = currentPosition
    }

    fun setTextViewDuration(duration: Int) {
        if (duration % 60 < 10) {
            textView_playerControl_duration.setText("${duration / 60}:0${duration % 60}")
        } else {
            textView_playerControl_duration.setText("${duration / 60}:${duration % 60}")
        }
    }

    fun setTextViewCurrentPostition(currentPosition: Int) {
        if (currentPosition % 60 < 10) {
            textView_playerControl_currentPosition.setText("${currentPosition / 60}:0${currentPosition % 60}")
        } else {
            textView_playerControl_currentPosition.setText("${currentPosition / 60}:${currentPosition % 60}")
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onPrevButtonPressed() {
        if (mListener != null) {
            mListener!!.onPlayPrevSong()
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
        fun onPlayPrevSong()
        fun onPlayNextSong()
        fun onDisplaySong()
        fun onPauseSong()
        fun onSeekToPosition(position: Int)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): PlayerControlFragment {
            val fragment = PlayerControlFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
