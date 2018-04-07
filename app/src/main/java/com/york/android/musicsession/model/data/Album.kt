package com.york.android.musicsession.model.data

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by York on 2018/4/3.
 */
@Parcelize
class Album(val name: String, val artist: String, val songs: ArrayList<Song>, val coverImageUrl: String) : Parcelable {
//    constructor(parcel: Parcel) : this(
//            parcel.readString(),
//            parcel.readString(),
//            parcel.readArrayList(Song::class.java.classLoader),
//            parcel.readString()) {
//    }
//
//    override fun writeToParcel(parcel: Parcel?, flags: Int) {
//        parcel?.writeString(name)
//        parcel?.writeString(artist)
//        parcel?.writeList(songs)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<Album> {
//        override fun createFromParcel(parcel: Parcel): Album {
//            return Album(parcel)
//        }
//
//        override fun newArray(size: Int): Array<Album?> {
//            return arrayOfNulls(size)
//        }
//    }
}