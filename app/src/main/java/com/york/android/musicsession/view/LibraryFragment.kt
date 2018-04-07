package com.york.android.musicsession.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.york.android.musicsession.R
import com.york.android.musicsession.view.albumpage.AlbumPageFragment
import com.york.android.musicsession.view.artistpage.ArtistPageFragment
import com.york.android.musicsession.view.songpage.SongPageFragment
import kotlinx.android.synthetic.main.fragment_discover.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LibraryFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LibraryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LibraryFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    val fragments = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }

        (activity as AppCompatActivity).setSupportActionBar(toolbar_discover)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onStart() {
        super.onStart()
        setupViewPager()
        initTabLayout()
    }

    fun initTabLayout() {
        if(tabLayout_discover.tabCount == 0) {
//            val tabSpecial = tabLayout_discover.newTab()
//            val tabCharts = tabLayout_discover.newTab()
//            val tabNewPublic = tabLayout_discover.newTab()
//            val tabStyle = tabLayout_discover.newTab()

//            tabLayout_discover.addTab(tabSpecial.setText("精選"))
//            tabLayout_discover.addTab(tabStyle.setText("排行榜"))
//            tabLayout_discover.addTab(tabCharts.setText("新發行"))
//            tabLayout_discover.addTab(tabNewPublic.setText("曲風情境"))
        }

        tabLayout_discover.setupWithViewPager(viewPager_discover, true)
//        tabLayout_discover.setupWithViewPager()
    }

    fun setupViewPager() {
        val fragments = ArrayList<Fragment>()

        fragments.add(SongPageFragment.newInstance("", ""))
        fragments.add(AlbumPageFragment.newInstance("", ""))
        fragments.add(ArtistPageFragment.newInstance("", ""))

        viewPager_discover.adapter = FragmentAdapter(fragments, childFragmentManager)
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

    inner class FragmentAdapter(val fragments: List<Fragment>, fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            var title = "無"
            when(position) {
                0 -> return "歌曲"
                1 -> return "專輯"
                2 -> return "演唱者"
            }
            return title
        }
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
         * @return A new instance of fragment LibraryFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): LibraryFragment {
            val fragment = LibraryFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
