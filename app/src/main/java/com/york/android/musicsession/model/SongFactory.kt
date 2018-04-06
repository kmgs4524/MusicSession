package com.york.android.musicsession.model

import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentActivity
import com.york.android.musicsession.model.data.Song
import java.util.*

/**
 * Created by York on 2018/4/2.
 */
class SongFactory(val activity: FragmentActivity) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSongs(keyword: String, type: String): List<Song> {
        var cursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                MediaStore.Audio.Media.IS_MUSIC + "=?",
                arrayOf("1"),
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
        val path = arrayOfNulls<String>(count)
        val artist = arrayOfNulls<String>(count)
        val duration = arrayOfNulls<Long>(count)

        if(cursor.moveToFirst()) {
            var i = 0
            do {
                names[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                path[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                artist[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                duration[i] = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                i++
            } while (cursor.moveToNext())
        }
        cursor.close()  // release cursor's resource after query

        val songs = ArrayList<Song>()
        for(i in 0 until names.size) {
//            Log.d("SongsFactory", "names[${i}]: ${names[i]}")
            songs.add(Song(names[i]!!, artist[i]!!, 0, duration[i]!!, path[i]!!))
        }

//        Log.d("SongsFragment", "songs: ${songs[0]} audioPath: ${audioPath[0]}")
        return songs
    }
}