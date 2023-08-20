package com.projectcitybuild.pcbridge.core.modules.config

import com.projectcitybuild.pcbridge.core.storage.adapters.JsonStorage

class Config<T>(
    private val default: T,
    private val jsonStorage: JsonStorage<T>,
) {
    private var cache: T? = null

    fun get(): T? {
        if (cache != null) {
            return cache
        }
        return (jsonStorage.read() ?: default)
            .also { cache = it }
    }

    fun flush() {
        cache = null
        get()
    }
}
