package com.projectcitybuild.modules.config.implementations

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.ConfigProvider
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class BungeecordConfig(
    private val plugin: Plugin
): ConfigProvider {

    private var config: Configuration? = null
    private fun getFile(): File = File(plugin.dataFolder, "config.yml")

    fun load() {
        config = null

        val file = getFile()

        createIfNeeded(file)

        try {
            config = ConfigurationProvider
                .getProvider(YamlConfiguration::class.java)
                .load(file)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createIfNeeded(file: File) {
        if (!file.exists()) {
            file.mkdir()
            try {
                file.createNewFile()
                javaClass.getResourceAsStream(file.name).use { input ->
                    FileOutputStream(file).use { output ->
                        ByteStreams.copy(input, output)
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException("Unable to create configuration file", e)
            }
        }
    }

    fun save() {
        ConfigurationProvider
            .getProvider(YamlConfiguration::class.java)
            .save(config, getFile())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: PluginConfig.ConfigPath<T>): T {
        val config = config ?: throw Exception("Attempted to read config file without loading it")
        return config.get(key.key) as? T ?: key.defaultValue
    }

    override fun get(path: String): Any? {
        val config = config ?: throw Exception("Attempted to read config file without loading it")
        return config.get(path)
    }

    override fun <T> set(key: PluginConfig.ConfigPath<T>, value: T) {
        val config = config ?: throw Exception("Attempted to read config file without loading it")

        config.set(key.key, value)
        save()
    }

    override fun addDefaults(vararg keys: PluginConfig.ConfigPath<*>) {
        val config = config ?: throw Exception("Attempted to read config file without loading it")

        keys.forEach { key ->
            if (config.get(key.key) == null)
                config.set(key.key, key.defaultValue)
        }
        save()
    }
}