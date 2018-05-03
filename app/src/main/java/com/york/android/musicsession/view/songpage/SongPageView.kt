package com.york.android.musicsession.view.songpage

import com.york.android.musicsession.model.data.Song

/**
 * Created by York on 2018/5/3.
 */
interface SongPageView {
    fun setParameters()
    fun initRecyclerView(songs: List<Song>)
    fun verifyStoragePermission()
}