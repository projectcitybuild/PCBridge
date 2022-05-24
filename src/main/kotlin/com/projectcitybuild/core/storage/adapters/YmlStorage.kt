package com.projectcitybuild.core.storage.adapters

import com.projectcitybuild.core.storage.Storage
import com.projectcitybuild.core.storage.StoragePath
import org.bukkit.configuration.file.FileConfiguration

class YmlStorage(
    private val config: FileConfiguration
) : Storage {

    override fun <T> get(key: StoragePath<T>): T {
        val value = config.get(key.key) as T
        if (value != null) {
            return value
        }
        return key.defaultValue
    }

    override fun <T> set(key: StoragePath<T>, value: T) {
        return config.set(key.key, value)
    }

    override fun get(path: String): Any? {
        return config.get(path)
    }
}
