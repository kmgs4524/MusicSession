package com.york.android.musicsession.model.datafactory

import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentActivity
import android.util.Log
//import com.kkbox.openapideveloper.auth.Auth
import com.york.android.musicsession.model.data.Artist
import com.york.android.musicsession.model.jsonconverter.ArtistConverter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import java.util.ArrayList

/**
 * Created by York on 2018/4/7.
 */
class ArtistFactory(val activity: FragmentActivity) {
    // KKBOX client
    val CLIENT_ID = "aa2dd565a7ae293236cb81e3e2497fe2"
    val CLIENT_SECRET = "a3c20ad364b0ace2c52ca6b8f428796f"

    @RequiresApi(Build.VERSION_CODES.O)
    fun getArtists(keyword: String, type: String): List<Artist> {


        var cursor = activity.contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_ALBUMS),
                "",
                null,
                "")
        when (type) {
            "Title" -> {
                cursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                        MediaStore.Audio.Media.TITLE + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
            "DiplayName" -> {
                cursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                        MediaStore.Audio.Media.DISPLAY_NAME + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
            "Album" -> {
                cursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                        MediaStore.Audio.Media.ALBUM + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
            "Artist" -> {
                cursor = activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC),
                        MediaStore.Audio.Media.ARTIST + "=?",   // selection formatted as an SQL WHERE clause
                        arrayOf(keyword),
                        "LOWER(${MediaStore.Audio.Media.TITLE}) ASC")
            }
        }

        val count = cursor.count    // number of rows

        val names = arrayOfNulls<String>(count)
        val albumCount = arrayOfNulls<String>(count)

        if (cursor.moveToFirst()) {
            var i = 0
            do {
                names[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
                albumCount[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
                i++
            } while (cursor.moveToNext())
        }
        cursor.close()  // release cursor's resource after query

        val artists = ArrayList<Artist>()
        val albumFactory = AlbumFactory(activity)


        for (i in 0 until names.size) {
            val albums = albumFactory.getAlbums(names[i]!!, "Artist")
            val artist = Artist(names[i]!!, albums, "")

            artists.add(artist)
        }

        Log.d("ArtistFactory", "artists size: ${artists.size} ")
        return artists
    }

}