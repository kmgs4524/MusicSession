package com.york.android.musicsession.model.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by York on 2018/4/6.
 */
@Parcelize
class Artist(val name: String, val albums: List<Album>, val imageUrl: String) : Parcelable {
}