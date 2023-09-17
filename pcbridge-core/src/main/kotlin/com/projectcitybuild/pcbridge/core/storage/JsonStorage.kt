package com.projectcitybuild.pcbridge.core.storage

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import java.io.File
import java.io.PrintWriter
import java.lang.Exception

class JsonStorage<T>(
    private val file: File,
    private val logger: PlatformLogger,
    private val typeToken: TypeToken<T>,
) {
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .disableHtmlEscaping()
        .create()

    fun read(): T? {
        return try {
            // Normally we'd pass a FileReader+JsonReader to gson to read the file contents,
            // but it's unable to interpret ASCII characters. For some reason, feeding a
            // JSON string to it instead doesn't have the same problem...
            var jsonString = ""
            file.inputStream().bufferedReader().forEachLine { jsonString += it }
            gson.fromJson(jsonString, typeToken.type)
        } catch (error: Exception) {
            logger.severe("Failed to deserialize from json: ${error.message}")
            null
        }
    }

    fun write(data: T) {
        return try {
            PrintWriter(file).use { writer ->
                gson.toJson(data, typeToken.type, writer)
            }
        } catch (error: Exception) {
            logger.severe("Failed to serialize to json: ${error.message}")
        }
    }
}
