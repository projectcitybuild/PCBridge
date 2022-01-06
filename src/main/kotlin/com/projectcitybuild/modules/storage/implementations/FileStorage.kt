package com.projectcitybuild.modules.storage.implementations

import com.projectcitybuild.modules.storage.Storage
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class FileStorage<T>(
    private val folderPath: File,
    private val encode: (YamlConfiguration, T) -> Unit,
    private val decode: (YamlConfiguration) -> T
): Storage<T> {
    private fun fileName(key: String) : String = "$key.yml"

    override suspend fun load(key: String): T? {
        val file = File(folderPath, fileName(key))
        if (!folderPath.exists()) folderPath.mkdir()
        if (!file.exists()) {
            runCatching {
                file.createNewFile()
            }.onFailure { throwable ->
                throwable.printStackTrace()
                return null
            }
        }

        val config = YamlConfiguration.loadConfiguration(file)
        return decode(config)
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

        val config = YamlConfiguration.loadConfiguration(file)
        encode(config, value)
    }

    override suspend fun delete(key: String) {
        val file = File(folderPath, fileName(key))
        file.delete()
    }
}