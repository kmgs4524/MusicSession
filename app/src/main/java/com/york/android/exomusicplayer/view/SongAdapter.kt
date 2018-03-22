package com.york.android.exomusicplayer.view

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.york.android.exomusicplayer.service.PlayService
import com.york.android.exomusicplayer.R
import com.york.android.exomusicplayer.model.Song
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by York on 2018/3/20.
 */
class SongAdapter(val songs: List<Song>, val context: Context, var service: Service?, val handler: Handler): RecyclerView.Adapter<SongAdapter.SongItemHolder>() {

    override fun onBindViewHolder(holder: SongItemHolder?, position: Int) {
        holder?.bind(songs[position])
        holder?.itemView?.setOnClickListener {
            Log.d("bind", "position: ${position}")
            (context as MainActivity).verifyStoragePermission()
            setService(songs[position])
//            (service as PlayService).initExoPlayer(songs[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SongItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.songitem_recyclerview, parent, false)
        val holder = SongItemHolder(view)

        return holder
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun setService(song: Song) {
        val intent = Intent()

        // start and bind service
        intent.setClass(context, PlayService::class.java)
        (context as MainActivity).startService(intent)
        context.bindService(intent, MusicServiceConnection(song), 0)
    }



    inner class SongItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(song: Song) {
            val textViewName = itemView.findViewById<TextView>(R.id.textView_songItem_name)
            val textViewArtist = itemView.findViewById<TextView>(R.id.textView_songItem_artist)

            textViewName.setText(song.name)
            textViewArtist.setText(song.artist)
        }
    }

    inner class MusicServiceConnection(var song: Song): ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            Log.d("onServiceConnected", "p0: ${p0}, binder: ${binder}")
            service = (binder as PlayService.LocalBinder).getService()
            (service as PlayService).initExoPlayer(song)
            (context as MainActivity).playerView_main.player = (service as PlayService).player
            Log.d("onServiceConnected", "player: ${context.playerView_main.player}")

            (service as PlayService).uiHandler = handler
        }
    }
}