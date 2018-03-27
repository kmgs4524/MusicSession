package com.york.android.exomusicplayer.view

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.transition.Explode
import android.support.transition.Transition
import android.support.transition.TransitionInflater
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.*
import com.york.android.exomusicplayer.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_player_control.*

class MainActivity : AppCompatActivity(), PlayerControlFragment.OnFragmentInteractionListener, DiscoverFragment.OnFragmentInteractionListener,
        SpecialFragment.OnFragmentInteractionListener, ChartsFragment.OnFragmentInteractionListener, NewPublicFragment.OnFragmentInteractionListener,
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
