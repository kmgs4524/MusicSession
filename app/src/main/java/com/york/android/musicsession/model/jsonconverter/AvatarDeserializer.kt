package com.york.android.musicsession.model.jsonconverter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Created by York on 2018/4/8.
 */
class AvatarDeserializer: JsonDeserializer<String> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): String {
        val dataArray = json?.asJsonObject?.get("artists")?.asJsonObject?.get("data")?.asJsonArray
        val urlString = dataArray!![0].asJsonObject.get("images").asJsonArray[0].asJsonObject.get("url").asString

        return urlString
    }
}