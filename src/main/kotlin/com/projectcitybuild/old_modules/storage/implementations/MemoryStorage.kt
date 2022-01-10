package com.projectcitybuild.old_modules.storage.implementations

import com.projectcitybuild.old_modules.storage.Storage

class MemoryStorage<T>: Storage<T> {
    private val storage = HashMap<String, T>()

    override suspend fun load(key: String): T? {
        return storage[key]
    }

    override suspend fun save(key: String, value: T) {
        storage[key] = value
    }

    override suspend fun delete(key: String) {
        storage.remove(key)
    }
}