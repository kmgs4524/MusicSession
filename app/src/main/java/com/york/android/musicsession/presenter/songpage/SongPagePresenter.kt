package com.york.android.musicsession.presenter.songpage

import android.os.Build
import android.support.annotation.RequiresApi
import com.google.android.gms.tasks.Tasks.await
import com.york.android.musicsession.model.datafactory.SongFactory
import com.york.android.musicsession.model.songpage.GetSongInDevice
import com.york.android.musicsession.view.songpage.SongPageFragment
import com.york.android.musicsession.view.songpage.SongPageView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.custom.async

/**
 * Created by York on 2018/5/3.
 */
class SongPagePresenter(val songPageView: SongPageView, val songFactory: SongFactory) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun onGetSongData() {
        async(UI) {
            val getSongs = async(CommonPool) {
                songFactory.getSongs("", "")
            }
            val songs = getSongs.await()
            songPageView.initRecyclerView(songs)
        }
    }

    fun onVerifyPermission() {
        songPageView.verifyStoragePermission()
    }
}