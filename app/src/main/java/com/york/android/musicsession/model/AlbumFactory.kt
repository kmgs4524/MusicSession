package com.york.android.musicsession.model

import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.york.android.musicsession.model.data.Album
import com.york.android.musicsession.model.data.Song
import java.util.ArrayList

/**
 * Created by York on 2018/4/6.
 */
class AlbumFactory(val activity: FragmentActivity) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAlbums(keyword: String, type: String): List<Album> {
        var cursor = activity.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM_ART),
                "",
                null,
                "")

        when(type) {
            "Title" -> {
                cursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA),
                        MediaStore.Audio.Media.TITLE + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
            "DiplayName" -> {
                cursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA),
                        MediaStore.Audio.Media.DISPLAY_NAME + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
        }

        val count = cursor.count    // number of rows

        val names = arrayOfNulls<String>(count)
        val artist = arrayOfNulls<String>(count)
        val albumArts = arrayOfNulls<String>(count)

        if(cursor.moveToFirst()) {
            var i = 0
            do {
                names[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
                artist[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST))
                albumArts[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
                i++
            } while (cursor.moveToNext())
        }
        cursor.close()  // release cursor's resource after query

        var songs = ArrayList<Song>()
        val albums = ArrayList<Album>()
        val songFactory = SongFactory(activity) // used to get songs of specified artist

        Log.d("AlbumFactory", "names size: ${names.size}")
        for(i in 0 until names.size) {
            songs = songFactory.getSongs(names[i]!!, "Album") as ArrayList<Song>
//            Log.d("AlbumFactory", "name : ${names[i]} artist: ${artist[i]} song size: ${songs.size} albumArt: ${albumArts[i]}")

            if(albumArts[i] == null) {
                albums.add(Album(names[i]!!, artist[i]!!, songs, ""))
            } else {
                albums.add(Album(names[i]!!, artist[i]!!, songs, albumArts[i]!!))
            }
        }

//        Log.d("SongsFragment", "songs: ${songs[0]} audioPath: ${audioPath[0]}")
        return albums
    }
}