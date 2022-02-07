package com.projectcitybuild.features.hub

import com.projectcitybuild.entities.LegacyWarp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Deprecated("Will be deleted in next version")
class HubFileStorage(
    private val folderPath: File,
) {
    private val fileName = "hub.json"

    fun load(): LegacyWarp? {
        val file = File(folderPath, fileName)
        if (!folderPath.exists()) folderPath.mkdir()
        if (!file.exists()) {
            return null
        }
        val json = file.readLines().joinToString()
        if (json.isEmpty())
            return null

        return Json.decodeFromString<LegacyWarp>(string = json)
    }

    fun save(value: LegacyWarp) {
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