package com.york.android.musicsession.view.artist

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Album

/**
 * Created by York on 2018/4/8.
 */
class ArtistAlbumAdapter(val artistAlbums: List<Album>, val context: Context): RecyclerView.Adapter<ArtistAlbumAdapter.ArtistAlbumHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ArtistAlbumHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.albumitem_recyclerview_artist, parent, false)
        val holder = ArtistAlbumHolder(view)

        return holder
    }

    override fun getItemCount(): Int {
        return artistAlbums.size
    }

    override fun onBindViewHolder(holder: ArtistAlbumHolder?, position: Int) {
        holder?.bind(artistAlbums[position])
    }

    inner class ArtistAlbumHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(album: Album) {
            val imageViewAlbumArtwork = itemView.findViewById<ImageView>(R.id.imageView_albumitemArtist_albumArtwork)
            val textViewAlbumName = itemView.findViewById<TextView>(R.id.textView_albumitemArtist_albumName)

            imageViewAlbumArtwork.setImageBitmap(BitmapFactory.decodeFile(album.coverImageUrl))
            textViewAlbumName.setText(album.name)
        }
    }
}