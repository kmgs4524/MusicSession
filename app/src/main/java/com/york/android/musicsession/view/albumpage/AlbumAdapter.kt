package com.york.android.musicsession.view.albumpage

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.york.android.musicsession.R
import com.york.android.musicsession.model.bitmap.BitmapCompression
import com.york.android.musicsession.model.data.Album
import com.york.android.musicsession.view.album.AlbumFragment
import org.jetbrains.anko.runOnUiThread

/**
 * Created by York on 2018/3/27.
 */
class AlbumAdapter(val albums: List<Album>, val context: Context): RecyclerView.Adapter<AlbumAdapter.RankItemHolder>() {
    companion object {
        val ENTER_ALBUM_STATE = "AlbumFragment"
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RankItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.albumitem_recyclerview_albumpage, parent, false)
        return RankItemHolder(view)
    }

    override fun onBindViewHolder(holder: RankItemHolder?, position: Int) {
        holder?.bind(albums[position])
        holder?.itemView?.setOnClickListener {
            val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()

            transaction.replace(R.id.constraintLayout_main_mainContainer, AlbumFragment.newInstance(albums[position]))
            transaction.addToBackStack(ENTER_ALBUM_STATE)
            transaction.commit()
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    inner class RankItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(album: Album) {
            val textViewArtistName = itemView.findViewById<TextView>(R.id.textView_albumItem_artist)
            val textViewAlbumName = itemView.findViewById<TextView>(R.id.textView_albumItem_name)
            val imageViewAlbumArt = itemView.findViewById<ImageView>(R.id.imageVIew_albumItem_albumArt)

            textViewAlbumName.setText(album.name)
            textViewArtistName.setText(album.artist)
            // load album image on background thread
            Thread(Runnable {
                if(album.coverImageUrl != "") {
                    Log.d("RandItemHolder", "album: ${album.name} imageUrl: ${album.coverImageUrl}")
                    val coverBitmap = BitmapCompression.compressBySize(album.coverImageUrl, 178, 210)
                    Log.d("RandItemHolder", "album: ${album.name} coverBitmap: ${coverBitmap}")
                    context.runOnUiThread {
                        imageViewAlbumArt.setImageBitmap(coverBitmap)
                    }
                }
            }).start()
        }
    }
}