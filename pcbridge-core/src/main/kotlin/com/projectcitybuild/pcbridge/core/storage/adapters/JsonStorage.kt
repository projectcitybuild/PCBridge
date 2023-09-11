package com.projectcitybuild.pcbridge.core.storage.adapters

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.lang.Exception

class JsonStorage<T>(
    private val file: File,
    private val logger: PlatformLogger,
    private val typeToken: TypeToken<T>,
) {
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    fun read(): T? {
        return try {
            FileReader(file).use { fileReader ->
                JsonReader(fileReader).use { jsonReader ->
                    gson.fromJson(jsonReader, typeToken.type)
                }
            }
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
