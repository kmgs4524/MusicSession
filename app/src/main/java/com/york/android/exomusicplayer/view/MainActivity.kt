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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.york.android.exomusicplayer.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_player_control.*

class MainActivity : AppCompatActivity(), PlayerControlFragment.OnFragmentInteractionListener, DiscoverFragment.OnFragmentInteractionListener,
        SpecialFragment.OnFragmentInteractionListener, ChartsFragment.OnFragmentInteractionListener, NewPublicFragment.OnFragmentInteractionListener,
        StyleFragment.OnFragmentInteractionListener, PlayerControlDialogFragment.Listener {
    override fun onPlayerControlClicked(position: Int) {

    }

    val bottomFragment = PlayerControlFragment.newInstance("", "")
    val discoverFragment = DiscoverFragment.newInstance("", "")

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val transition = supportFragmentManager.beginTransaction()

        transition.add(R.id.main_container, discoverFragment)
        transition.add(R.id.fragment_container, bottomFragment)
        transition.addToBackStack(null)
        transition.commit()

//        button_main_enter.setOnClickListener {
//            val intent = Intent()
//            intent.setClass(this, AlbumActivity::class.java)
//
//            val view = findViewById<View>(R.id.imageView_main)
//            val option = ActivityOptions.makeSceneTransitionAnimation(this, view, "album")
//            startActivity(intent, option.toBundle())
//        }
    }

    override fun onStart() {
        super.onStart()
        // bottom sheet fragment

    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
