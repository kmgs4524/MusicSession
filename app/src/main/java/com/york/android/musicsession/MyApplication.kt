package com.york.android.musicsession

import android.app.Application
import com.facebook.stetho.Stetho
import timber.log.Timber

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}