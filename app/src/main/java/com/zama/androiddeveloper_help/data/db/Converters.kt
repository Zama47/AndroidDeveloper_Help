package com.zama.androiddeveloper_help.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class Converters {

    private val listSerializer = ListSerializer(String.serializer())
    private val json = Json

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        json.encodeToString(listSerializer, value ?: emptyList())

    @TypeConverter
    fun toStringList(value: String): List<String> =
        runCatching { json.decodeFromString(listSerializer, value) }.getOrDefault(emptyList())
}
