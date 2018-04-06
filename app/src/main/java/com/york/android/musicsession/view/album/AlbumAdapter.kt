package com.york.android.musicsession.view.album

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.york.android.musicsession.R
import com.york.android.musicsession.model.Album
import com.york.android.musicsession.model.Rank

/**
 * Created by York on 2018/3/27.
 */
class AlbumAdapter(val albums: List<Album>, val context: Context): RecyclerView.Adapter<AlbumAdapter.RankItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RankItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.albumitem_recyclerview, parent, false)
        return RankItemHolder(view)
    }

    override fun onBindViewHolder(holder: RankItemHolder?, position: Int) {
        holder?.bind(albums[position])
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    inner class RankItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(album: Album) {
            val imageView = itemView.findViewById<ImageView>(R.id.imageVIew_albumItem)
            val textViewAlbumName = itemView.findViewById<TextView>(R.id.textView_albumItem_name)
        }
    }
}