package com.projectcitybuild.platforms.bungeecord.environment

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.entities.PluginConfig
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File

class BungeecordConfig(private val plugin: Plugin): ConfigProvider {

    override fun <T> get(key: PluginConfig.Pair<T>): T {
        // FIXME: stop IO thrashing
        val file = File(plugin.dataFolder, "config.yml")

        val config = ConfigurationProvider
                .getProvider(YamlConfiguration::class.java)
                .load(file)

        return config.get(key.key) as T // FIXME
    }

    override fun <T> set(key: PluginConfig.Pair<T>, value: T) {
        // FIXME: stop IO thrashing
        val file = File(plugin.dataFolder, "config.yml")

        val config = ConfigurationProvider
                .getProvider(YamlConfiguration::class.java)
                .load(file)

        config.set(key.key, value)

        ConfigurationProvider
                .getProvider(YamlConfiguration::class.java)
                .save(config, file)
    }
}