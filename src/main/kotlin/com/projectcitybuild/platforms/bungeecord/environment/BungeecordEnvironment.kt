package com.projectcitybuild.platforms.bungeecord.environment

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.entities.LogLevel
import com.projectcitybuild.core.entities.Player
import com.projectcitybuild.core.entities.PluginConfigPair
import com.projectcitybuild.core.utilities.AsyncTask
import com.projectcitybuild.core.utilities.Cancellable
import net.luckperms.api.LuckPerms
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class BungeecordEnvironment(
        private val plugin: Plugin,
        private val logger: Logger
) : EnvironmentProvider {

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.VERBOSE -> logger.info(message)    // spigot doesn't log FINEST level properly
            LogLevel.DEBUG -> logger.info(message)      // spigot doesn't log FINE level properly
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARNING -> logger.warning(message)
            LogLevel.FATAL -> logger.severe(message)
        }
    }

    override fun get(key: PluginConfigPair): Any {
        // FIXME: stop IO thrashing
        val file = File(plugin.dataFolder, "config.yml")

        val config = ConfigurationProvider
                .getProvider(YamlConfiguration::class.java)
                .load(file)

        return config.get(key.key)
    }

    override fun set(key: PluginConfigPair, value: Any) {
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

    override fun get(player: UUID): Player? {
        TODO()
//        return playerStore.get(player)
    }

    override fun set(player: Player) {
        TODO()
//        playerStore.put(player.uuid, player)
    }

    override fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T> {
        // Bukkit/Spigot performs Asynchronous units of work via their internal Scheduler
        return AsyncTask<T> { resolve ->
            val runnable = Runnable {
                task { result -> resolve(result) }
            }
            val bukkitTask = plugin.proxy?.scheduler?.runAsync(plugin, runnable)

            Cancellable {
                bukkitTask?.cancel()
            }
        }
    }

    override fun sync(task: () -> Unit) {
        val runnable = Runnable { task() }
        plugin.proxy?.scheduler?.schedule(plugin, runnable, 0, TimeUnit.NANOSECONDS)
    }

    override val permissions: LuckPerms? by lazy {
        TODO("Not implemented yet")
    }
}