package com.projectcitybuild.modules.storage

class FileStorage<T>: Storage<T> {
    override suspend fun load(key: String): T? {
        TODO("Not yet implemented")
    }

    override suspend fun save(key: String, value: T) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(key: String) {
        TODO("Not yet implemented")
    }
}