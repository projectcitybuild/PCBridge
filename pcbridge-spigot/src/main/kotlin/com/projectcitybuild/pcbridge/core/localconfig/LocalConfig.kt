package com.projectcitybuild.pcbridge.core.localconfig

import com.projectcitybuild.pcbridge.data.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.data.default

class LocalConfig(
    private val jsonStorage: JsonStorage<LocalConfigKeyValues>,
) {
    private var cache: LocalConfigKeyValues? = null

    fun get(): LocalConfigKeyValues {
        val cache = cache
        if (cache != null) {
            return cache
        }
        return (jsonStorage.read() ?: LocalConfigKeyValues.default())
            .also { this.cache = it }
    }

    fun flush() {
        cache = null
        get()
    }
}
