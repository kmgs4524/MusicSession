package com.york.android.musicsession.view.playercontrol

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.york.android.musicsession.R
import com.york.android.musicsession.model.bitmap.BlurBuilder
import com.york.android.musicsession.model.datafactory.SongFactory
import kotlinx.android.synthetic.main.fragment_player_control.*


class PlayerControlFragment : Fragment() {

    // TODO: Rename and change types of parameters
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
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_player_control, container, false)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        setBlurBackground()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setBlurBackground() {
        val songs = SongFactory(activity).getSongs("吳汶芳", "Artist")
        if(songs[0].coverImageUrl != "none") {
            Log.d("PlayerControlFragment", "coverImageUrl: ${songs[0].coverImageUrl}")
            val coverBitmap = BitmapFactory.decodeFile(songs[0].coverImageUrl)
            Log.d("PlayerControlFragment", "coverBitmap: ${coverBitmap}")
            val blurredBitmap = BlurBuilder().blur(coverBitmap, activity)
            Log.d("PlayerControlFragment", "blurredBitmap: ${blurredBitmap}")
            constrainLayout_playerControl_container.background = BitmapDrawable(resources, blurredBitmap)
//            constraintLayout_controlView.background = BitmapDrawable(resources, blurredBitmap)
            imageView_playerControl_artwork.setImageBitmap(coverBitmap)
        }
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
