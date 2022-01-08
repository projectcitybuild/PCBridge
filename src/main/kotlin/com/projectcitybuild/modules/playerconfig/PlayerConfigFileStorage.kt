package com.projectcitybuild.modules.playerconfig

import com.projectcitybuild.entities.PlayerConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PlayerConfigFileStorage(
    private val folderPath: File,
) {
    private fun fileName(key: String) : String = "$key.json"

    suspend fun load(key: String): PlayerConfig? {
        val file = File(folderPath, fileName(key))
        if (!folderPath.exists()) folderPath.mkdir()
        if (!file.exists()) {
            return null
        }
        val json = file.readLines().toString()
        return Json.decodeFromString(string = json)
    }

    suspend fun save(key: String, value: PlayerConfig) {
        val file = File(folderPath, fileName(key))
        if (!folderPath.exists()) folderPath.mkdir()
        if (!file.exists()) {
            runCatching {
                file.createNewFile()
            }.onFailure { e ->
                e.printStackTrace()
                return
            }
        }
        val json = Json.encodeToString(value = value)
        file.writeText(json)
    }

    suspend fun delete(key: String) {
        val file = File(folderPath, fileName(key))
        file.delete()
    }
}