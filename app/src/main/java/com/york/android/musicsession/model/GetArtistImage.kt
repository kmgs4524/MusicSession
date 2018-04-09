package com.york.android.musicsession.model

import android.content.Context
import android.util.Log
import com.kkbox.openapideveloper.auth.Auth
import com.york.android.musicsession.model.jsonconverter.ArtistConverter
import com.york.android.musicsession.view.MainActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by York on 2018/4/9.
 */
class GetArtistImage(val context: Context) {
    // KKBOX client
    private val CLIENT_ID = "aa2dd565a7ae293236cb81e3e2497fe2"
    private val CLIENT_SECRET = "a3c20ad364b0ace2c52ca6b8f428796f"

    fun getImage(name: String): String {
        val client = OkHttpClient()
        val auth = Auth(CLIENT_ID, CLIENT_SECRET, context)
        val accessToken = auth.clientCredentialsFlow.fetchAccessToken().get().get("access_token").asString
        val request = Request.Builder().url("https://api.kkbox.com/v1.1/search?q=${name}&type=artist&territory=TW")
                .addHeader("accept", "application/json")
                .addHeader("authorization", "Bearer ${accessToken}")
                .build()
        val response = client.newCall(request).execute()
        val result = response.body()?.string()
        lateinit var imageUrl: String
        if (result != null) {
            val converter = ArtistConverter()
            imageUrl = converter.convertToImageUrl(result)
            Log.d("GetArtistImage", "imageUrl: ${imageUrl}")
        }

        return imageUrl
    }
}