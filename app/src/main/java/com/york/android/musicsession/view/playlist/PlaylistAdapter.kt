package com.york.android.musicsession.view.playlist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Playlist
import kotlinx.android.synthetic.main.playlistitem_recyclerview.view.*

/**
 * Created by York on 2018/4/10.
 */
class PlaylistAdapter(val playlists: List<Playlist>, val context: Context): RecyclerView.Adapter<PlaylistAdapter.PlaylistItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PlaylistItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.playlistitem_recyclerview, parent,false)
        val holder = PlaylistItemHolder(view)

        return holder
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun onBindViewHolder(holder: PlaylistItemHolder?, position: Int) {
        holder?.bind(playlists[position])
        holder?.itemView?.setOnClickListener {

        }
    }

    inner class PlaylistItemHolder(itemView: View?): RecyclerView.ViewHolder(itemView) {
        fun bind(playlist: Playlist) {
            val textViewPlaylistName = itemView.findViewById<TextView>(R.id.textView_playlistItem_playlistName)
            val textViewSongCount = itemView.findViewById<TextView>(R.id.textView_playlistItem_songCount)
//            val imageViewPlaylistIcon = itemView.findViewById<ImageView>(R.id.imageView_playlistItem_icon)

            textViewPlaylistName.setText(playlist.name)
            textViewSongCount.setText("${playlist.songCount} 首歌曲")
        }
    }
}