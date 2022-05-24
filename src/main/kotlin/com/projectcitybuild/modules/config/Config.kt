package com.projectcitybuild.modules.config

import com.projectcitybuild.core.storage.Storage
import com.projectcitybuild.core.storage.StoragePath
import com.projectcitybuild.modules.config.adapters.StorageBackedKeys
import javax.inject.Inject

class Config @Inject constructor(
    private val storage: Storage,
) {
    class CachedStorage(
        private val storage: Storage,
    ): Storage {
        val cache: MutableMap<String, Any> = mutableMapOf()

        override fun <T> get(path: StoragePath<T>): T {
            val cached = cache[path.key]
            if (cached != null) {
                return cached as T
            }
            return storage.get(path)
                .also { cache[path.key] = it as Any }
        }

        override fun <T> set(path: StoragePath<T>, value: T) {
            cache[path.key] = value as Any
            storage.set(path, value)
        }

        override fun get(path: String): Any? {
            val cached = cache[path]
            if (cached != null) {
                return cached
            }
            return storage.get(path)
                .also { cache[path] = it as Any }
        }
    }

    private val cachedStorage = CachedStorage(storage)

    val keys = StorageBackedKeys(storage = cachedStorage)

    fun flush() {
        cachedStorage.cache.clear()
    }
}