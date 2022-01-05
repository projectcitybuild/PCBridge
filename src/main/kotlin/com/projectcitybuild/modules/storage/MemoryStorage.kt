package com.projectcitybuild.modules.storage

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