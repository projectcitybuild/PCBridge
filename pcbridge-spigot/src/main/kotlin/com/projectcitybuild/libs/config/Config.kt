package com.projectcitybuild.libs.config

class Config(
    private val keyValueStorage: KeyValueStorage,
) {
    private val cache: MutableMap<String, Any> = mutableMapOf()

    fun <T> get(key: ConfigStorageKey<T>): T {
        val cached = cache[key.path]
        if (cached != null) {
            return cached as T
        }
        return keyValueStorage.get(key)
            .also { cache[key.path] = it as Any }
    }

    fun <T : Any> set(key: ConfigStorageKey<T>, value: T) {
        keyValueStorage.set(key, value)
        cache[key.path] = value
    }

    fun flush() {
        cache.clear()
    }
}
