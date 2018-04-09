package com.york.android.musicsession.model.bitmap

import android.graphics.BitmapFactory
import android.R.attr.src
import android.graphics.Bitmap
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by York on 2018/4/9.
 */
class GetBitmapFromUrl {
    fun getBitmap(urlString: String): Bitmap? {
        try {
            val url = URL(urlString)
            Log.d("GetBitmapFromUrl", "url: ${url}")
            val connection = url.openConnection() as HttpURLConnection
            Log.d("GetBitmapFromUrl", "connection: ${connection}")
            connection.setDoInput(true)
            connection.connect()
            val input = connection.getInputStream()
            Log.d("GetBitmapFromUrl", "input: ${input}")
            val bitmap = BitmapFactory.decodeStream(input)
            Log.d("GetBitmapFromUrl", "bitmap: ${bitmap}")

            return bitmap
        } catch (e: IOException) {
            // Log exception
            return null
        }

    }
}