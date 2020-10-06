package com.york.android.musicsession.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.york.android.musicsession.R
import com.york.android.musicsession.view.albumpage.AlbumPageFragment
import com.york.android.musicsession.view.artistpage.ArtistPageFragment
import com.york.android.musicsession.view.songpage.SongPageFragment
import kotlinx.android.synthetic.main.fragment_discover.*

class LibraryFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar_discover)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.actions, menu)
    }

    override fun onStart() {
        super.onStart()
        setupViewPager()
        initTabLayout()
    }

    private fun initTabLayout() {
        tabLayout_discover.setupWithViewPager(viewPager_discover, true)
    }

    private fun setupViewPager() {
        val fragments = ArrayList<Fragment>()

        fragments.add(SongPageFragment.newInstance())
        fragments.add(ArtistPageFragment.newInstance())

        viewPager_discover.adapter = FragmentAdapter(fragments, childFragmentManager)
    }

    inner class FragmentAdapter(private val fragments: List<Fragment>, fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            val title = "無"
            when (position) {
                0 -> return "歌曲"
                1 -> return "演唱者"
            }
            return title
        }
    }

    companion object {
        fun newInstance(): LibraryFragment =
            LibraryFragment()
    }
}
