package com.york.android.exomusicplayer.view

import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.view.*
import com.york.android.exomusicplayer.R
import com.york.android.exomusicplayer.view.mymusic.MyMusicFragment
import com.york.android.exomusicplayer.view.playercontrol.PlayerControlDialogFragment
import com.york.android.exomusicplayer.view.playercontrol.PlayerControlFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PlayerControlFragment.OnFragmentInteractionListener, DiscoverFragment.OnFragmentInteractionListener,
        SpecialFragment.OnFragmentInteractionListener, ChartsFragment.OnFragmentInteractionListener,
        StyleFragment.OnFragmentInteractionListener, MyMusicFragment.OnFragmentInteractionListener,
        PlayerControlDialogFragment.Listener {

    override fun onPlayerControlClicked(position: Int) {

    }

    val bottomFragment = PlayerControlFragment.newInstance("", "")
    val myMusicFragment = MyMusicFragment.newInstance("", "")
    val discoverFragment = DiscoverFragment.newInstance("", "")

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val transition:FragmentTransaction = supportFragmentManager.beginTransaction()

        transition.add(R.id.main_container, discoverFragment)
        transition.add(R.id.fragment_container, bottomFragment)
        transition.addToBackStack(null)
        transition.commit()

        setDrawerListener()
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

    fun setDrawerListener() {
        navigationDrawer_main.setNavigationItemSelectedListener({item: MenuItem ->
            val transition:FragmentTransaction = supportFragmentManager.beginTransaction()

            when(item.itemId) {
                R.id.nav_mymusic ->   transition.replace(R.id.main_container, myMusicFragment)
                R.id.nav_dicover -> transition.replace(R.id.main_container, discoverFragment)
                else -> true
            }
            transition.addToBackStack(null)
            transition.commit()

            drawerLayout_main.closeDrawers()
            true
        })
    }

    override fun onStart() {
        super.onStart()
        // bottom sheet fragment

    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
