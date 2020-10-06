package com.york.android.musicsession.view.artistpage

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.york.android.musicsession.R
import com.york.android.musicsession.model.datafactory.ArtistFactory
import kotlinx.android.synthetic.main.fragment_artistpage.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.runOnUiThread

class ArtistPageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_artistpage, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        initRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initRecyclerView() {
        Thread(Runnable {
            val artists = ArtistFactory(activity).getArtists("", "")
            context.runOnUiThread {
                progressBar_artistPage_loading.visibility = View.GONE
                recyclerView_artistPage.layoutManager = LinearLayoutManager(activity)
                recyclerView_artistPage.adapter = ArtistAdapter(artists, activity)
                recyclerView_artistPage.addItemDecoration(DividerItemDecoration(activity, LinearLayout.VERTICAL))
            }
        }).start()
    }

    companion object {
        fun newInstance(): ArtistPageFragment =
            ArtistPageFragment()
    }
}
