package com.projectcitybuild.core.storage.adapters

import com.projectcitybuild.core.storage.Storage
import com.projectcitybuild.core.storage.StoragePath
import org.bukkit.configuration.file.FileConfiguration

class YmlStorage(
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

    override fun get(path: String): Any? {
        return config.get(path)
    }
}
