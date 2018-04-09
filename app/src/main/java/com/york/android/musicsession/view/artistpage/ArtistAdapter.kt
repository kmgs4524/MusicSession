package com.york.android.musicsession.view.artistpage

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.york.android.musicsession.R
import com.york.android.musicsession.model.GetArtistImage
import com.york.android.musicsession.model.data.Artist
import com.york.android.musicsession.view.MainActivity
import com.york.android.musicsession.view.artist.ArtistFragment
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

/**
 * Created by York on 2018/4/6.
 */
class ArtistAdapter(val artists: List<Artist>, val activity: Context): RecyclerView.Adapter<ArtistAdapter.ArtistItemHolder>() {
    companion object {
        val ENTER_ARTIST_STATE = "ArtistFragment"
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ArtistItemHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.artistitem_recyclerview, parent, false)
        val holder = ArtistItemHolder(view)

        return holder
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    override fun onBindViewHolder(holder: ArtistItemHolder?, position: Int) {
        holder?.bind(artists[position])
        holder?.itemView?.setOnClickListener {
            val transaction = (activity as MainActivity).supportFragmentManager.beginTransaction()

            Log.d("ArtistAdapter", "artist iamgeUrl: ${artists[position].imageUrl}")
            transaction.replace(R.id.constraintLayout_main_mainContainer, ArtistFragment.newInstance(artists[position]))
            transaction.addToBackStack(ENTER_ARTIST_STATE)
            transaction.commit()
        }
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