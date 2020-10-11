package com.york.android.musicsession.view.exoplayer

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.view.MainActivity

/**
 * Created by York on 2018/3/20.
 */
class SongAdapter(val songs: List<Song>, val context: Context): RecyclerView.Adapter<SongAdapter.SongItemHolder>() {

    override fun onBindViewHolder(holder: SongItemHolder?, position: Int) {
        holder?.bind(songs[position])
        holder?.itemView?.setOnClickListener {
            Log.d("bind", "position: ${position} song path: ${songs[position].filePath}")
//            (context as MainActivity).verifyStoragePermission()
            (context as MainActivity).setPlaylist(songs, position)
            context.showBottomPlayerControl()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SongItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.songitem_recyclerview, parent, false)
        return SongItemHolder(view)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    inner class SongItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(song: Song) {
            val textViewName = itemView.findViewById<TextView>(R.id.textView_songItem_name)
            val textViewArtist = itemView.findViewById<TextView>(R.id.textView_songItem_artist)
            // split filename extension
            val tokens = song.name.split(".")

            textViewName.text = tokens[0]
            textViewArtist.text = song.artist
        }
    }
}