package com.projectcitybuild.modules.playerconfig

import com.projectcitybuild.modules.storage.Storage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

class JSONFileStorage<T>(
    private val folderPath: File,
    private val serializer: KSerializer<T>
): Storage<T> {
    private fun fileName(key: String) : String = "$key.json"

    override suspend fun load(key: String): T? {
        val file = File(folderPath, fileName(key))
        if (!folderPath.exists()) folderPath.mkdir()
        if (!file.exists()) {
            return null
        }

        val json = file.readLines().toString()
        return Json.decodeFromString(serializer, json)
    }

    override suspend fun save(key: String, value: T) {
        val file = File(folderPath, fileName(key))
        if (!folderPath.exists()) folderPath.mkdir()
        if (!file.exists()) {
            runCatching {
                file.createNewFile()
            }.onFailure { throwable ->
                throwable.printStackTrace()
                return
            }
        }

        val json = Json.encodeToString(serializer, value)
        file.writeText(json)
    }

    override suspend fun delete(key: String) {
        val file = File(folderPath, fileName(key))
        file.delete()
    }
}