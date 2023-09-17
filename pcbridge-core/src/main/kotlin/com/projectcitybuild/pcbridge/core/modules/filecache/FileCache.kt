package com.projectcitybuild.pcbridge.core.modules.filecache

import com.projectcitybuild.pcbridge.core.storage.JsonStorage

class FileCache<T>(
    private val jsonStorage: JsonStorage<T>,
) {
    private var cache: T? = null

    fun get(): T? {
        return cache
            ?: jsonStorage.read().also { cache = it }
    }

    fun put(value: T) {
        cache = value
        jsonStorage.write(value)
    }
}