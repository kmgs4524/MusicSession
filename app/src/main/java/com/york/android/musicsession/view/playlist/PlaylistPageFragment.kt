package com.york.android.musicsession.view.playlist

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Playlist
import com.york.android.musicsession.model.data.Song
import kotlinx.android.synthetic.main.fragment_playlist_page.*

class PlaylistPageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_playlist_page, container, false)
    }

    override fun onStart() {
        super.onStart()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val playlists = ArrayList<Playlist>()

        playlists.add(Playlist("最常播放", ArrayList<Song>(), 22))
        playlists.add(Playlist("最近播放", ArrayList<Song>(), 22))
        recyclerView_playlistPage.layoutManager = LinearLayoutManager(activity)
        recyclerView_playlistPage.adapter = PlaylistAdapter(playlists, activity)
    }

    companion object {
        fun newInstance(): PlaylistPageFragment =
            PlaylistPageFragment()
    }
}
