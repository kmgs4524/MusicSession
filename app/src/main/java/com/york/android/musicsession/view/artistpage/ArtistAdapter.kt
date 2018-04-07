package com.york.android.musicsession.view.artistpage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Artist

/**
 * Created by York on 2018/4/6.
 */
class ArtistAdapter(val artists: List<Artist>, val context: Context): RecyclerView.Adapter<ArtistAdapter.ArtistItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ArtistItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.artistitem_recyclerview, parent, false)
        val holder = ArtistItemHolder(view)

        return holder
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    override fun onBindViewHolder(holder: ArtistItemHolder?, position: Int) {
        holder?.bind(artists[position])
    }

    inner class ArtistItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        fun bind(artist: Artist) {
            val textViewArtistName = itemView.findViewById<TextView>(R.id.textView_artistItem_name)
            val textViewAlbumCount = itemView.findViewById<TextView>(R.id.textView_artistItem_albumCount)

            textViewArtistName.setText(artist.name)
            textViewAlbumCount.setText("${artist.albums.size} 專輯")
        }

    }
}