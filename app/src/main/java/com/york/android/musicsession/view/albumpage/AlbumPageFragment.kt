package com.york.android.musicsession.view.albumpage

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
import com.york.android.musicsession.model.datafactory.AlbumFactory
import kotlinx.android.synthetic.main.fragment_albumpage.*

class AlbumPageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_albumpage, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        initRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initRecyclerView() {
        Thread(Runnable {
            val albums = AlbumFactory(activity).getAlbums("", "")
            activity.runOnUiThread {
                progressBar_albumPage_loading.visibility = View.GONE
                val layoutManager = GridLayoutManager(activity, 2)
                recyclerView_albumPage.layoutManager = layoutManager
                recyclerView_albumPage.adapter = AlbumAdapter(albums, activity)
            }
        }).start()
    }

    companion object {
        fun newInstance(): AlbumPageFragment =
            AlbumPageFragment()
    }
}
