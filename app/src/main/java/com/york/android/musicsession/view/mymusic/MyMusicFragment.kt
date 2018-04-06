package com.york.android.musicsession.view.mymusic

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.LibraryItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_my_music.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MyMusicFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MyMusicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyMusicFragment : Fragment() {

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
        return inflater!!.inflate(R.layout.fragment_my_music, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onStart() {
        super.onStart()
//        initToolbarTitle()
        initRecyclerView()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.home -> {
                drawerLayout_main.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    fun initToolbarTitle() {
//        val appCompatActivity = activity as AppCompatActivity
//        appCompatActivity.setSupportActionBar(toolbar_mymusic)
//        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        appCompatActivity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
//    }

    fun initRecyclerView() {
        val items = ArrayList<LibraryItem>()

        items.add(LibraryItem("全部歌曲", 0, 0, true))
        items.add(LibraryItem("可離線播放歌曲", 0, 0, true))
        items.add(LibraryItem("播放紀錄", 0, 0, true))
        items.add(LibraryItem("我的收藏", 0, 0, false))
        items.add(LibraryItem("收藏歌曲", 0, 0, true))
        items.add(LibraryItem("收藏專輯", 0, 0, true))
        items.add(LibraryItem("收藏歌單", 0, 0, true))
        items.add(LibraryItem("我的歌單", 0, 0, false))
        items.add(LibraryItem("已分享歌單", 0, 0, true))

        recyclerView_myMusic.layoutManager = LinearLayoutManager(context)
        recyclerView_myMusic.adapter = LibraryItemAdapter(items, activity)
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
         * @return A new instance of fragment MyMusicFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): MyMusicFragment {
            val fragment = MyMusicFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
