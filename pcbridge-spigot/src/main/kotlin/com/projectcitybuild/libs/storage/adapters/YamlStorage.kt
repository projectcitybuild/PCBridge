package com.projectcitybuild.libs.storage.adapters

import com.projectcitybuild.libs.storage.Storage
import com.projectcitybuild.libs.storage.StoragePath
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
