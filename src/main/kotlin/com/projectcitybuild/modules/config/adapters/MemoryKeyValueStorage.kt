package com.projectcitybuild.modules.config.adapters

import com.projectcitybuild.modules.config.ConfigStorageKey
import com.projectcitybuild.modules.config.KeyValueStorage

class MemoryKeyValueStorage: KeyValueStorage {

    private var data: MutableMap<String, Any> = mutableMapOf()

    override fun <T> get(key: ConfigStorageKey<T>): T {
        return data[key.path]?.let { it as T }
            ?: throw Exception("Value not mocked")
    }

    override fun <T> set(key: ConfigStorageKey<T>, value: T) {
        data[key.path] = value as Any
    }
}
