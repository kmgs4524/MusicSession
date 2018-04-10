package com.york.android.musicsession.view.exoplayer

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.exoplayer2.ui.PlayerControlView
import com.york.android.musicsession.service.PlayService
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.view.MainActivity
import kotlinx.android.synthetic.main.activity_album.*

/**
 * Created by York on 2018/3/20.
 */
class SongAdapter(val songs: List<Song>, val context: Context, var service: Service?, var connection: MusicServiceConnection?, val handler: Handler): RecyclerView.Adapter<SongAdapter.SongItemHolder>() {
    init {
        setService(songs)
        Log.d("onBindViewHolder", "init: setService")
    }

    override fun onBindViewHolder(holder: SongItemHolder?, position: Int) {
        holder?.bind(songs[position])
        holder?.itemView?.setOnClickListener {
            Log.d("bind", "position: ${position} song path: ${songs[position].filePath}")
//            (context as MainActivity).verifyStoragePermission()
            (service as PlayService).playMediaSource(position)
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

    fun setService(song: List<Song>) {
        val intent = Intent()
        val connection = MusicServiceConnection(songs)

        // start and bind service
        intent.setClass(context, PlayService::class.java)
        (context as AppCompatActivity).startService(intent)
        context.bindService(intent, connection, 0)

    }

    fun unbindService() {
        service?.unbindService(connection)
    }

    inner class SongItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(song: Song) {
            val textViewName = itemView.findViewById<TextView>(R.id.textView_songItem_name)
            val textViewArtist = itemView.findViewById<TextView>(R.id.textView_songItem_artist)
            // split filename extension
            val tokens = song.name.split(".")

            textViewName.setText(tokens[0])
            textViewArtist.setText(song.artist)
        }
    }

    inner class MusicServiceConnection(var songs: List<Song>): ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            Log.d("onServiceConnected", "p0: ${p0}, binder: ${binder}")
            service = (binder as PlayService.LocalBinder).getService()
            (service as PlayService).createConcatenatingMediaSource(songs)
//            ((context as AppCompatActivity).playerView_album as PlayerControlView).player = (service as PlayService).player
//            Log.d("onServiceConnected", "player: ${(context as AppCompatActivity).playerView_album.player}")

            (service as PlayService).uiHandler = handler
        }
    }
}