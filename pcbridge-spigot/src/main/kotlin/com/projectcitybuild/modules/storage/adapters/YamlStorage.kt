package com.projectcitybuild.modules.storage.adapters

import com.projectcitybuild.modules.storage.Storage
import com.projectcitybuild.modules.storage.StoragePath
import org.bukkit.configuration.file.FileConfiguration

class YamlStorage(
    private val config: FileConfiguration
) : Storage {

    override fun <T> get(path: StoragePath<T>): T {
        val value = config.get(path.key) as T
        if (value != null) {
            return value
        }
        return path.defaultValue
    }

    override fun <T> set(path: StoragePath<T>, value: T) {
        config.set(path.key, value)
    }
}
