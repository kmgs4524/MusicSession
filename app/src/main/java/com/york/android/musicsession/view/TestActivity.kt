package com.york.android.musicsession.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.kkbox.openapideveloper.api.Api
import com.kkbox.openapideveloper.auth.Auth
import com.york.android.musicsession.R
import kotlinx.android.synthetic.main.activity_test.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync

class TestActivity : AppCompatActivity() {
    val CLIENT_ID = "aa2dd565a7ae293236cb81e3e2497fe2"
    val CLIENT_SECRET = "a3c20ad364b0ace2c52ca6b8f428796f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val auth = Auth(CLIENT_ID, CLIENT_SECRET, this)
        val accessToken = auth.clientCredentialsFlow.fetchAccessToken().get().get("access_token").asString
        val api = Api(accessToken, "TW", this)

        val client = OkHttpClient()



        doAsync {
//            api.albumFetcher.setAlbumId ("KmRKnW5qmUrTnGRuxF")
//            val searchResult = api.searchFetcher.fetchSearchResult(50, 0).get().get(accessToken)
//            val result = api.albumFetcher.fetchTracks(10, 0).get().get(accessToken).asString

//            val queryString = "吳汶芳"
//            val request = Request.Builder().url("https://api.kkbox.com/v1.1/search?q=${queryString}&type=track&territory=TW")
//                    .addHeader("accept", "application/json")
//                    .addHeader("authorization", "Bearer ${accessToken}")
//                    .build()
//            val response = client.newCall(request).execute()
//            val str = response.body()?.string()

            val str = "\u7121\u7aae (Endlessness)"
            Log.d("TestActivity", "response: ${str}")
            runOnUiThread { textView.setText(str) }
        }
//        val searchResult = api.searchFetcher.fetchSearchResult(10, 2).get().get(accessToken)

    }
}
