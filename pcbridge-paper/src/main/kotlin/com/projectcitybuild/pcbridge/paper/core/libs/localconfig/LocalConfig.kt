package com.projectcitybuild.pcbridge.paper.core.libs.localconfig

import com.projectcitybuild.pcbridge.paper.core.libs.storage.JsonStorage
import java.io.File

class LocalConfig(
    private val file: File,
    private val jsonStorage: JsonStorage<LocalConfigKeyValues>,
) {
    private var cache: LocalConfigKeyValues? = null

    // TODO: this should really be suspending
    fun get(): LocalConfigKeyValues {
        val cache = cache
        if (cache != null) {
            return cache
        }
        return (jsonStorage.readSync(file) ?: LocalConfigKeyValues.default())
            .also { this.cache = it }
    }

    fun flush() {
        cache = null
        get()
    }
}
