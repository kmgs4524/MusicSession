package com.york.android.musicsession.view.albumtab

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.york.android.musicsession.R
import com.york.android.musicsession.model.AlbumFactory
import kotlinx.android.synthetic.main.fragment_album.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AlbumFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AlbumFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlbumFragment : Fragment() {

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
        return inflater!!.inflate(R.layout.fragment_album, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        initRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initRecyclerView() {
        val albums = AlbumFactory(activity).getAlbums("", "")

//        albums.add(Album("天堂/懸崖", "李佳薇", ArrayList<Song>(), ""))
//        albums.add(Album("我來自", "吳汶芳", ArrayList<Song>(), ""))
//        albums.add(Album("我來自", "吳汶芳", ArrayList<Song>(), ""))
//        albums.add(Album("我來自", "吳汶芳", ArrayList<Song>(), ""))
//        albums.add(Album("我來自", "吳汶芳", ArrayList<Song>(), ""))
//        albums.add(Album("我來自", "吳汶芳", ArrayList<Song>(), ""))
//        albums.add(Album("我來自", "吳汶芳", ArrayList<Song>(), ""))
//        albums.add(Album("我來自", "吳汶芳", ArrayList<Song>(), ""))

        val layoutManager = GridLayoutManager(activity, 2)
        recyclerView_album.layoutManager = layoutManager
        recyclerView_album.adapter = AlbumAdapter(albums, activity)
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlbumFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): AlbumFragment {
            val fragment = AlbumFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
