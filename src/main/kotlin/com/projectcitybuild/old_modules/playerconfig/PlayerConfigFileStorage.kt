package com.projectcitybuild.old_modules.playerconfig

import com.projectcitybuild.entities.PlayerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        val json = file.readLines().joinToString()
        if (json.isEmpty())
            return null

        return Json.decodeFromString<PlayerConfig>(string = json)
    }

    suspend fun save(key: String, value: PlayerConfig) {
        val file = File(folderPath, fileName(key))
        if (!folderPath.exists()) folderPath.mkdir()
        if (!file.exists()) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    file.createNewFile()
                }.onFailure { e ->
                    e.printStackTrace()
                    return@launch
                }
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