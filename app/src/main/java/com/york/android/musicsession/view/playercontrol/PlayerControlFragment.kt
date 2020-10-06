package com.york.android.musicsession.view.playercontrol

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.york.android.musicsession.R
import com.york.android.musicsession.model.bitmap.BitmapCompression
import com.york.android.musicsession.model.bitmap.BlurBuilder
import com.york.android.musicsession.view.MainActivity
import kotlinx.android.synthetic.main.fragment_player_control.*
import org.jetbrains.anko.backgroundColor


class PlayerControlFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_control, container, false)
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

        imageView_playerControl_shuffleMode.setOnClickListener {
            (activity as MainActivity).onShuffleModeEnable()
        }

        seekBar_playerControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("PlayerControlFragment", "onStopTrackingTouch")
                (activity as MainActivity).onSeekToPosition(seekBar_playerControl.progress)
            }
        })
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
        if (imageUrl != "") {
            Thread(Runnable {
                Log.d("setAlbumArtwork", "imageUrl: ${imageUrl}")
                val bitmap = BitmapCompression.compressBySize(imageUrl, 200, 200)
                val smallBitmap = BitmapFactory.decodeFile(imageUrl)
                Log.d("setAlbumArtwork", "smallBitmap: ${smallBitmap}")
                activity.runOnUiThread {
                    Log.d("setAlbumArtwork", "smallBitmap: ${smallBitmap}")
                    imageView_playerControl_artwork.setImageBitmap(bitmap)
                    imageView_playerControl_artworkSmall.setImageBitmap(smallBitmap)
                }
            }).start()
        }
    }

    fun setArtistName(artistName: String) {
        textView_playerControl_artistName.text = artistName
        textView_playerControl_artistNameTitle.text = artistName
    }

    fun setSongName(songName: String) {
        textView_playerControl_songName.text = songName
        textView_playerControl_songNameTitle.text = songName
    }

    fun setDuration(duration: Int) {
        Log.d("PlayerControlFragment", "duration: ${duration}")
        if (duration % 60 < 10) {
            textView_playerControl_duration.text = "${duration / 60}:0${duration % 60}"
        } else {
            textView_playerControl_duration.text = "${duration / 60}:${duration % 60}"
        }
        seekBar_playerControl.max = duration
    }

    private fun setCurrentPosition(currentPosition: Int) {
        activity.runOnUiThread {
            if (currentPosition % 60 < 10) {
                textView_playerControl_currentPosition.text = "${currentPosition / 60}:0${currentPosition % 60}"
            } else {
                textView_playerControl_currentPosition.text = "${currentPosition / 60}:${currentPosition % 60}"
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
            currentPosition += timeDelta * playbackState.playbackSpeed.toLong()
            Log.d("PlayerControlFragment", "currentPosition : ${currentPosition} timeDelta: ${timeDelta} playbackSpeed: ${playbackState.playbackSpeed.toLong()}")
            setCurrentPosition((currentPosition!! / 1000).toInt())
        }
    }

    fun changeShuffleIconBackground(shuffleMode: Int) {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            imageView_playerControl_shuffleMode.backgroundColor = ContextCompat.getColor(activity, R.color.material_grey_600)
        } else {
            imageView_playerControl_shuffleMode.backgroundColor = ContextCompat.getColor(activity, R.color.transparent)
        }
    }

    interface OnFragmentInteractionListener {
        fun onPlayPrevSong()
        fun onPlayNextSong()
        fun onDisplaySong()
        fun onPauseSong()
        fun onSeekToPosition(position: Int)
        fun onShuffleModeEnable()
    }

    companion object {
        const val PLAY_CONTROL_FRAGMENT = "PLAY_CONTROL_FRAGMENT"

        fun newInstance(): PlayerControlFragment =
            PlayerControlFragment()
    }
}
