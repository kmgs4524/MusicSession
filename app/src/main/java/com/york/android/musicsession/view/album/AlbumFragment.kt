package com.york.android.musicsession.view.album

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.york.android.musicsession.R
import com.york.android.musicsession.model.data.Album
import com.york.android.musicsession.model.data.Song
import com.york.android.musicsession.view.exoplayer.SongAdapter
import kotlinx.android.synthetic.main.fragment_album.*
import java.util.ArrayList

class AlbumFragment : Fragment(), AlbumView {

    private var album: Album? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_album, container, false)
    }

    override fun onStart() {
        super.onStart()
        album?.let {
            setAlbumNameArtist(it.name, it.artist)
            setAlbumArt(it.coverImageUrl)
            initRecyclerView(it.songs)
        }
    }

    private fun setAlbumNameArtist(albumName: String, artist: String) {
        textView_album_albumName.text = albumName
        textView_album_artist.text = artist
    }

    private fun setAlbumArt(albumArtPath: String) {
        imageView_album_artwork.setImageBitmap(BitmapFactory.decodeFile(albumArtPath))
    }

    private fun initRecyclerView(songs: ArrayList<Song>) {
        // UI Handler
        songs.forEach {
            Log.d("AlbumFragment", "name: ${it.name} path: ${it.filePath}")
        }
        recyclerView_album.layoutManager = LinearLayoutManager(activity)
        recyclerView_album.adapter = SongAdapter(songs, activity)
    }

    companion object {
        private const val ALBUM = "ALBUM"

        fun newInstance(album: Album): AlbumFragment =
            AlbumFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ALBUM, album)
                }
            }
    }
}
