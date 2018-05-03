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
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.model.datafactory.SongFactory
import com.york.android.musicsession.presenter.songpage.SongPagePresenter
import com.york.android.musicsession.view.exoplayer.SongAdapter
import kotlinx.android.synthetic.main.fragment_songs.*

class SongPageFragment : Fragment(), SongPageView {
    // verify permission
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    private lateinit var presenter: SongPagePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setParameters()
        presenter = SongPagePresenter(this, SongFactory(activity))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_songs, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        presenter.onVerifyPermission()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d("SongPageFragment", "grantResults: ${grantResults}")
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            presenter.onGetSongData()
        }
    }

    override fun setParameters() {
        arguments.let {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun verifyStoragePermission() {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        } else {
            Log.d("SongPageFragment", "state permission")
            presenter.onGetSongData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initRecyclerView(songs: List<Song>) {
        Log.d("SongPageFragment", "songs: ${songs}")
        activity.runOnUiThread {
            progressBar_songPage_loading.visibility = View.GONE
            recyclerView_songs.layoutManager = LinearLayoutManager(activity)
            recyclerView_songs.adapter = SongAdapter(songs, activity)
            recyclerView_songs.addItemDecoration(DividerItemDecoration(activity, LinearLayout.VERTICAL))
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
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): SongPageFragment {
            val fragment = SongPageFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
