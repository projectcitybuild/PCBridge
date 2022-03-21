package com.projectcitybuild.modules.config.implementations

import com.google.common.io.ByteStreams
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Singleton

@Singleton
class BungeecordConfig(
    private val dataFolder: File
) : PlatformConfig {

    private val file: File
        get() = File(dataFolder, "config.yml")

    private val config: Configuration by lazy {
        createIfNeeded(file)

        val config = ConfigurationProvider
            .getProvider(YamlConfiguration::class.java)
            .load(file)
            .also { generateDefaultConfig(it) }

        config
    }

    private fun generateDefaultConfig(config: Configuration) {
        arrayOf(
            ConfigKey.API_ENABLED,
            ConfigKey.API_KEY,
            ConfigKey.API_BASE_URL,
            ConfigKey.API_IS_LOGGING_ENABLED,
            ConfigKey.WARPS_PER_PAGE,
            ConfigKey.TP_REQUEST_AUTO_EXPIRE_SECONDS,
            ConfigKey.DB_HOSTNAME,
            ConfigKey.DB_PORT,
            ConfigKey.DB_NAME,
            ConfigKey.DB_USERNAME,
            ConfigKey.DB_PASSWORD,
            ConfigKey.ERROR_REPORTING_SENTRY_ENABLED,
            ConfigKey.ERROR_REPORTING_SENTRY_DSN,
            ConfigKey.GROUPS_APPEARANCE_ADMIN_DISPLAY_NAME,
            ConfigKey.GROUPS_APPEARANCE_ADMIN_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_SOP_DISPLAY_NAME,
            ConfigKey.GROUPS_APPEARANCE_SOP_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_OP_DISPLAY_NAME,
            ConfigKey.GROUPS_APPEARANCE_OP_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_MODERATOR_DISPLAY_NAME,
            ConfigKey.GROUPS_APPEARANCE_MODERATOR_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_TRUSTEDPLUS_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_TRUSTED_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_DONOR_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_ARCHITECT_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_ENGINEER_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_PLANNER_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_BUILDER_HOVER_NAME,
            ConfigKey.GROUPS_APPEARANCE_INTERN_HOVER_NAME,
        ).forEach { key ->
            if (config.get(key.key) == null)
                config.set(key.key, key.defaultValue)
        }
        save(config)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: ConfigKey.ConfigPath<T>): T {
        val config = config
        return config.get(key.key) as? T ?: key.defaultValue
    }

    override fun get(path: String): Any? {
        val config = config
        return config.get(path)
    }

    override fun <T> set(key: ConfigKey.ConfigPath<T>, value: T) {
        val config = config

        config.set(key.key, value)
        save(config)
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

    private fun save(config: Configuration) {
        ConfigurationProvider
            .getProvider(YamlConfiguration::class.java)
            .save(config, file)
    }
}
