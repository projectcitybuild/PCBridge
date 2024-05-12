package com.projectcitybuild.pcbridge.core.config

import com.projectcitybuild.pcbridge.data.PluginConfig
import com.projectcitybuild.pcbridge.data.default

class Config(
    private val jsonStorage: JsonStorage<PluginConfig>,
) {
    private var cache: PluginConfig? = null

    fun get(): PluginConfig {
        val cache = cache
        if (cache != null) {
            return cache
        }
        return (jsonStorage.read() ?: PluginConfig.default())
            .also { this.cache = it }
    }

    fun flush() {
        cache = null
        get()
    }
}
