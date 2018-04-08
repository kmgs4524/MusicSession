package com.york.android.musicsession.model.jsonconverter

import com.google.gson.GsonBuilder

/**
 * Created by York on 2018/4/8.
 */
class ArtistConverter {
    fun convertToImageUrl(jsonString: String): String {
        val gson = GsonBuilder().registerTypeAdapter(String::class.java, AvatarDeserializer())
                .create()
        val imageUrl = gson.fromJson<String>(jsonString, String::class.java)

        return imageUrl
    }
}