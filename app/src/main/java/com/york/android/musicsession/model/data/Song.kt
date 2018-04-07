package com.york.android.musicsession.model.data

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by York on 2018/3/20.
 */

@Parcelize
class Song(val name: String, val artist: String, val coverImageUrl: Int, val duration: Long, val filePath: String) : Parcelable {
}