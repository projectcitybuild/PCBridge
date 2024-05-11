package com.projectcitybuild.core.config

import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.data.default

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