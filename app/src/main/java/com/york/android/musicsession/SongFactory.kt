package com.york.android.musicsession

import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.york.android.musicsession.model.Song

/**
 * Created by York on 2018/4/2.
 */
class SongFactory(val activity: AppCompatActivity) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSongs(keyword: String, type: String): List<Song> {
        var cursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION),
                null,
                null)
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
        val duration = arrayOfNulls<String>(count)

        if(cursor.moveToFirst()) {
            var i = 0
            do {
                names[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                path[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                artist[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                duration[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
            } while (cursor.moveToNext())
        }
        cursor.close()  // release cursor's resource after query

        val songs = ArrayList<Song>()
        for(i in 0 until names.size) {
            songs.add(Song(names[i], "", ))
        }

        Log.d("SongsFragment", "songs: ${songs[0]} audioPath: ${audioPath[0]}")
    }
}