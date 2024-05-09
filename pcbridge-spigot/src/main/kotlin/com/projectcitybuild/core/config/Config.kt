package com.projectcitybuild.core.config

import com.projectcitybuild.data.PluginConfig
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import java.nio.file.Path

class Config(
    path: Path,
) {
    private var cached: PluginConfig? = null

    private val loader = GsonConfigurationLoader.builder()
        .path(path)
        .defaultOptions { options ->
            options.serializers { builder ->
                builder.registerAnnotatedObjects(objectMapperFactory())
            }
        }
        .build()

    fun load(): PluginConfig {
        if (cached != null) {
            cached
        }
        val node = loader.load()
        val config: PluginConfig? = node.get()
        checkNotNull(config) {
            "Config failed to load or file not found"
        }
        cached = config
        return config
    }
}