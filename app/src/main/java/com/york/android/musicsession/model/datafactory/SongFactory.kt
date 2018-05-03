package com.york.android.musicsession.model.datafactory

import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.york.android.musicsession.model.data.Song
import java.util.*

/**
 * Created by York on 2018/4/2.
 */
class SongFactory(val activity: FragmentActivity) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSongs(keyword: String, type: String): List<Song> {
        val songs = ArrayList<Song>()

        var mediaCursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                MediaStore.Audio.Media.IS_MUSIC + "=?",
                arrayOf("1"),
                "")
        when (type) {
            "Title" -> {
                mediaCursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                        MediaStore.Audio.Media.TITLE + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
            "DiplayName" -> {
                mediaCursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                        MediaStore.Audio.Media.DISPLAY_NAME + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
            "Album" -> {
                mediaCursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                        MediaStore.Audio.Media.ALBUM + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
            "Artist" -> {
                mediaCursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                        MediaStore.Audio.Media.ARTIST + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
        }

        val count = mediaCursor.count    // number of rows

        val names = arrayOfNulls<String>(count)
        val path = arrayOfNulls<String>(count)
        val artist = arrayOfNulls<String>(count)
        val album = arrayOfNulls<String>(count)
        val duration = arrayOfNulls<Long>(count)

        // get result from cursor
        if (mediaCursor.moveToFirst()) {
            var i = 0
            do {
                names[i] = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                path[i] = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                artist[i] = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                album[i] = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                duration[i] = mediaCursor.getLong(mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                i++
            } while (mediaCursor.moveToNext())
        }
        mediaCursor.close()  // release cursor's resource after query


        for (i in 0 until names.size) {
            Log.d("SongsFactory", "names[${i}]: ${names[i]}")
            songs.add(Song(names[i]!!, artist[i]!!, album[i]!!, getAlbumArtPath(album[i]!!), duration[i]!!, path[i]!!))
        }
//        Log.d("SongPageFragment", "songs: ${songs[0]} audioPath: ${audioPath[0]}")


        return songs
    }

    // get album artwork of song
    fun getAlbumArtPath(albumName: String): String {
        var albumCursor = activity.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums.ALBUM_ART),
                MediaStore.Audio.Media.ALBUM + "=?",
                arrayOf(albumName),
                "")

        albumCursor.moveToFirst()
        var albumArtPath = albumCursor.getString(albumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))

        if (albumArtPath == null) {
            albumArtPath = ""
        }
        Log.d("SongFactory", "albumArtPath: ${albumArtPath}")
        albumCursor.close()

        return albumArtPath
    }
}