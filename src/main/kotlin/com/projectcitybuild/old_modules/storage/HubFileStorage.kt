package com.projectcitybuild.old_modules.storage

import com.projectcitybuild.entities.Warp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class HubFileStorage(
    private val folderPath: File,
) {
    private val fileName = "hub.json"

    suspend fun load(): Warp? {
        val file = File(folderPath, fileName)
        if (!folderPath.exists()) folderPath.mkdir()
        if (!file.exists()) {
            return null
        }
        val json = file.readLines().joinToString()
        if (json.isEmpty())
            return null

        return Json.decodeFromString<Warp>(string = json)
    }

    suspend fun save(value: Warp) {
        val file = File(folderPath, fileName)
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
}