package com.york.android.musicsession.view.songpage

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.york.android.musicsession.R
import com.york.android.musicsession.model.datafactory.SongFactory
import com.york.android.musicsession.view.exoplayer.SongAdapter
import kotlinx.android.synthetic.main.fragment_songs.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SongPageFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SongPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongPageFragment : Fragment() {
    // verify permission
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null
    val handler = object: Handler() {
        override fun handleMessage(msg: Message?) {
            val data = msg?.data
//                Log.d("thread check", "current thread id: ${Thread.currentThread().id}")
            Log.d("handler", "data: ${data}")
            if(data != null) {
                Log.d("handler", "current position: ${data?.getInt("CURRENT_POSITION")} duration: ${data?.getInt("DURATION")}")
//                progressbar_album.progress = data?.getInt("CURRENT_POSITION")
//                progressbar_album.max = data?.getInt("DURATION")

//                textView_controlview_artist.setText(data?.getString("ARTIST"))
//                textView_controlview_songname.setText(data?.getString("SONG_NAME"))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }

    }

    fun verifyStoragePermission() {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d("SongPageFragment", "grantResults: ${grantResults}")
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initRecyclerView()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onStart() {
        super.onStart()
        verifyStoragePermission()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initRecyclerView() {
        val factory = SongFactory(activity)
        val songs = factory.getSongs("", "")

        recyclerView_songs.layoutManager = LinearLayoutManager(activity)
        recyclerView_songs.adapter = SongAdapter(songs, activity, null, null, handler)
        recyclerView_songs.addItemDecoration(DividerItemDecoration(activity, LinearLayout.VERTICAL))
    }

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
        fun newInstance(param1: String, param2: String): SongPageFragment {
            val fragment = SongPageFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
