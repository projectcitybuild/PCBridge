package com.projectcitybuild.pcbridge.paper.core.libs.storage

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.lang.Exception

class JsonStorage<T>(
    private val typeToken: TypeToken<T>,
): Storage<T> {
    private val gson =
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .disableHtmlEscaping()
            .create()

    override suspend fun read(file: File): T? = withContext(Dispatchers.IO) {
        readSync(file)
    }

    override suspend fun write(file: File, data: T) = withContext(Dispatchers.IO) {
        writeSync(file, data)
    }

    override fun readSync(file: File): T? {
        return try {
            if (!file.exists()) return null

            // Normally we'd pass a FileReader+JsonReader to gson to read the file contents,
            // but it's unable to interpret ASCII characters. For some reason, feeding a
            // JSON string to it instead doesn't have the same problem...
            var jsonString = ""
            file.inputStream().bufferedReader().forEachLine { jsonString += it }
            gson.fromJson(jsonString, typeToken.type)
        } catch (e: Exception) {
            // TODO: throw errors instead of handling them here
            log.error(e) { "Failed to deserialize from json" }
            null
        }
    }

    override fun writeSync(file: File, data: T) {
        return try {
            file.parentFile.mkdirs()
            file.createNewFile()

            PrintWriter(file).use { writer ->
                gson.toJson(data, typeToken.type, writer)
            }
        } catch (e: Exception) {
            // TODO: throw errors instead of handling them here
            log.error(e) { "Failed to serialize to json" }
        }
    }
}
