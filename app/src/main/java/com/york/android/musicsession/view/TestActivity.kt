package com.york.android.musicsession.view

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
//import com.google.android.youtube.player.*
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.york.android.musicsession.R
import kotlinx.android.synthetic.main.activity_test.*
import okhttp3.OkHttpClient
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class TestActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    // KKBOX client
    val CLIENT_ID = "aa2dd565a7ae293236cb81e3e2497fe2"
    val CLIENT_SECRET = "a3c20ad364b0ace2c52ca6b8f428796f"

    val playerFragment = YouTubePlayerSupportFragment()
    // Youtube Data
    lateinit var credential: GoogleAccountCredential    // manage authorization and selection for account
    val scopes = ArrayList<String>()    // store YoutubeScope string

    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }

    lateinit var progress: ProgressDialog

    var searchVideoIds = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // KKBOX access token and api
//        val auth = Auth(CLIENT_ID, CLIENT_SECRET, this)
//        val accessToken = auth.clientCredentialsFlow.fetchAccessToken().get().get("access_token").asString
//        val api = Api(accessToken, "TW", this)
        // used for KKBOX API
        val client = OkHttpClient()

        progress = ProgressDialog(this)
        progress.setMessage("Calling Youtube Data API...")

        scopes.add(YouTubeScopes.YOUTUBE_READONLY)  // scope of view Youtube account for use with Youtube Data API
        // create Google account credential
        credential = GoogleAccountCredential.usingOAuth2(this, scopes).setBackOff(ExponentialBackOff())

        setFragment()
        setButton()
//        doAsync {
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

//            val str = "\u7121\u7aae (Endlessness)"
//            Log.d("TestActivity", "response: ${str}")
//            runOnUiThread { textView_test.setText(str) }
//        }
//        val searchResult = api.searchFetcher.fetchSearchResult(10, 2).get().get(accessToken)

    }

    fun setFragment() {
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.container_test, playerFragment)
        transaction.commit()
    }

    inner class OnInitializedListener(val videoIds: List<String>) : YouTubePlayer.OnInitializedListener {

        override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, p2: Boolean) {
            Log.d("TestActivity", "onInitializationSuccess videoIds: ${videoIds}")
            player?.cueVideo("FqrzCxSWaZY")
        }

        override fun onInitializationFailure(provider: YouTubePlayer.Provider?, player: YouTubeInitializationResult?) {

        }

    }

    override fun onStart() {
        super.onStart()

    }

    fun setButton() {
        button_test_callapi.setOnClickListener {
            button_test_callapi.isEnabled = false
            textView_test.setText("")
            getResultFromApi()
            button_test_callapi.isEnabled = true
            textView_test.setText("Click the call api button to test Api")

        }
    }

    // check precondition: 1. Google Play Service is installed
    // 2. account was selected
    // 3. devices' network connectivity is available
    fun getResultFromApi() {
        if (!isGoogleServiceAvailable()) {
            acquireGooglePlayServices()
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            textView_test.setText("No network connection available.")
        } else {
            // all preconditions are satisfied
            MakeRequestTask(credential).execute()
            Log.d("TestActivity", "ids: ${searchVideoIds}")
        }
    }

    // check whether device's network connectivity exists and it's possible to pass data
    fun isDeviceOnline(): Boolean {
        // A ConnectivityManager for handling management of network connections.
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // details about the currently active default data network
        val networkInfo = connectivityManager.activeNetworkInfo

        return (networkInfo != null && networkInfo.isConnected)
    }

    fun isGoogleServiceAvailable(): Boolean {
        // helper class for verifying Google Play Services APK is available and up-to-date on this device
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)

        Log.d("TestActivity", "is success ${connectionStatusCode == ConnectionResult.SUCCESS}")
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)

        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    fun chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE).getString("PREF_ACCOUNT_NAME", null)
            // if user hasn't selected account, start newChooseAccountIntent
            if (accountName != null) {

                credential.setSelectedAccountName(accountName)
                getResultFromApi()
            } else {
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    "MusicSession needs access to your Google account.",
                    REQUEST_PERMISSION_GET_ACCOUNTS,    // request code
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> {
            }
            REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)

                    if (accountName != null) {
                        val settings = getPreferences(Context.MODE_PRIVATE)
                        val editor = settings.edit()

                        editor.putString("PREF_ACCOUNT_NAME", accountName)
                        editor.apply()
                        credential.setSelectedAccountName(accountName)
                        getResultFromApi()
                    }
                }
            }
            REQUEST_AUTHORIZATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    getResultFromApi()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    inner class MakeRequestTask : AsyncTask<Void, Void, com.york.android.musicsession.model.SearchResult> {

        private var service: YouTube

        constructor(credential: GoogleAccountCredential) {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()

            service = YouTube.Builder(transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Test")
                    .build()
        }

        override fun doInBackground(vararg p0: Void?): com.york.android.musicsession.model.SearchResult {
            val videoIds = getDataFromApi()
            val searchResult = com.york.android.musicsession.model.SearchResult(credential.token, videoIds)
            Log.d("TestActivity", "doInBackground searchVideIds: ${videoIds}")

            return searchResult
        }

        fun getDataFromApi(): List<String> {
//            val channelInfo = ArrayList<String>()
            val videoIds = ArrayList<String>()
            val search = service.search().list("id, snippet")   // collection of search results that match the query parameters specified in the API request.

            try {
//                val result = service.channels().list("snippet, contentDetails, statistics")
//                        .setForUsername("GoogleDevelopers")
//                        .execute()
//                val channels = result.items
//                if (channels != null) {
//                    val channel = channels.get(0)
//                    channelInfo.add("This channel's ID is ${channel.id}. Its title is '${channel.snippet.title}, and it has ")
//                }

                search.setQ("吳汶芳 無窮")
                        .setType("video")
                        .setFields("items(id/videoId)")
                        .setMaxResults(3)
                val searchResponse = search.execute()
                val resultItems = searchResponse.items

                resultItems.let {
                    it.forEach {
                        videoIds.add(it.id.videoId)
                    }
                }

            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
                startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            }

            return videoIds
        }

        override fun onPreExecute() {
            textView_test.setText("")
            progress.show()
        }

        override fun onPostExecute(output: com.york.android.musicsession.model.SearchResult?) {
            progress.dismiss()
            if (output == null || output.videoIds.size == 0) {
                textView_test.setText("No results returned.")
            } else {
                playerFragment.initialize(output.token, OnInitializedListener(output.videoIds))
                output.videoIds.forEach {
                    Log.d("TestActivity", it)
                }
                textView_test.setText(TextUtils.join("\n", output.videoIds))
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // do nothing
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // do nothing
    }

    override fun onPause() {
        super.onPause()
        progress.let {
            it.dismiss()
        }
    }
}
