package com.projectcitybuild.libs.config.adapters

import com.projectcitybuild.libs.config.ConfigStorageKey
import com.projectcitybuild.libs.config.KeyValueStorage

class MemoryKeyValueStorage : KeyValueStorage {

    private var data: MutableMap<String, Any> = mutableMapOf()

    override fun <T> get(key: ConfigStorageKey<T>): T {
        return data[key.path]?.let { it as T }
            ?: key.defaultValue
    }

    override fun <T> set(key: ConfigStorageKey<T>, value: T) {
        data[key.path] = value as Any
    }
}
