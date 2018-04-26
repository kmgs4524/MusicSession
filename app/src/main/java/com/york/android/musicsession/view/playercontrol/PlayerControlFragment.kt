package com.york.android.musicsession.view.playercontrol

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.*
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
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

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
        Thread(Runnable {
            if (imageUrl != "") {
                Log.d("PlayerControlFragment", "coverImageUrl: ${imageUrl}")
                val coverBitmap = BitmapFactory.decodeFile(imageUrl)
                Log.d("PlayerControlFragment", "coverBitmap: ${coverBitmap}")
                val blurredBitmap = BlurBuilder().blur(coverBitmap, activity)
                Log.d("PlayerControlFragment", "blurredBitmap: ${blurredBitmap}")
                activity.runOnUiThread {
                    constrainLayout_playerControl_container.background = BitmapDrawable(resources, blurredBitmap)
                    imageView_playerControl_artwork.setImageBitmap(coverBitmap)
                }
            }
        }).start()
    }

    fun setPlayIcon(state: Int) {
        Log.d("PlayerControlFragment", "stateBuilder: ${state}")
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            imageView_playerControl_pause.visibility = View.VISIBLE
            imageView_playerControl_play.visibility = View.GONE
        } else {
            imageView_playerControl_play.visibility = View.VISIBLE
            imageView_playerControl_pause.visibility = View.GONE
        }
    }

    fun setAlbumArtwork(imageUrl: String) {
        Log.d("setAlbumArtwork", "imageUrl: ${imageUrl}")
        if(imageUrl != "") {
            Thread(Runnable {
                val bitmap = BitmapCompression.compressBySize(imageUrl, 200, 200)
                activity.runOnUiThread {
                    imageView_playerControl_artwork.setImageBitmap(bitmap)
                    imageView_playerControl_artworkSmall.setImageBitmap(bitmap)
                }
            })
        }
    }

    fun setArtistName(artistName: String) {
        textView_playerControl_artistName.setText(artistName)
        textView_playerControl_artistNameTitle.setText(artistName)
    }

    fun setSongName(songName: String) {
        textView_playerControl_songName.setText(songName)
        textView_playerControl_songNameTitle.setText(songName)
    }

    fun setDuration(duration: Int) {
        Log.d("PlayerControlFragment", "duration: ${duration}")
        if (duration % 60 < 10) {
            textView_playerControl_duration.setText("${duration / 60}:0${duration % 60}")
        } else {
            textView_playerControl_duration.setText("${duration / 60}:${duration % 60}")
        }
        seekBar_playerControl.max = duration
    }

    fun setCurrentPostition(currentPosition: Int) {
        activity.runOnUiThread {
            if (currentPosition % 60 < 10) {
                textView_playerControl_currentPosition.setText("${currentPosition / 60}:0${currentPosition % 60}")
            } else {
                textView_playerControl_currentPosition.setText("${currentPosition / 60}:${currentPosition % 60}")
            }
            seekBar_playerControl.progress = currentPosition
        }
    }

    fun updateCurrentPosition(playbackState: PlaybackStateCompat) {
        var currentPosition = playbackState.position
        var timeDelta = (SystemClock.elapsedRealtime() - playbackState.lastPositionUpdateTime) // the time between last update time and current system time
        Log.d("PlayerControlFragment", "elapsedRealtime: ${SystemClock.elapsedRealtime()} lastPositionUpdateTime: ${playbackState.lastPositionUpdateTime}")
        Log.d("PlayerControlFragment", "currentPosition: ${currentPosition} timeDelta: ${timeDelta}")

        if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            currentPosition = currentPosition + timeDelta * playbackState.playbackSpeed.toLong()
            Log.d("PlayerControlFragment", "currentPosition : ${currentPosition} timeDelta: ${timeDelta} playbackSpeed: ${playbackState.playbackSpeed.toLong()}")
            setCurrentPostition((currentPosition!! / 1000).toInt())
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
        fun onPlayPrevSong()
        fun onPlayNextSong()
        fun onDisplaySong()
        fun onPauseSong()
        fun onSeekToPosition(position: Int)
    }

    companion object {
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
}
