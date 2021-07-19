package com.projectcitybuild.platforms.spigot.environment

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.utilities.PlayerStore
import com.projectcitybuild.core.utilities.AsyncTask
import com.projectcitybuild.core.utilities.Cancellable
import com.projectcitybuild.core.entities.LogLevel
import com.projectcitybuild.core.entities.Player
import com.projectcitybuild.core.entities.PluginConfigPair
import net.luckperms.api.LuckPerms
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference
import java.util.*
import java.util.logging.Logger

class SpigotEnvironment(
        private val pluginRef: WeakReference<JavaPlugin>,
        private val logger: Logger,
        private val playerStore: PlayerStore,
        private val config: FileConfiguration,
        private val hooks: com.projectcitybuild.platforms.spigot.environment.SpigotPluginHook
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
        return config.get(key.key)
    }

    override fun set(key: PluginConfigPair, value: Any) {
        config.set(key.key, value)
    }

    override fun get(player: UUID): Player? {
        return playerStore.get(player)
    }

    override fun set(player: Player) {
        playerStore.put(player.uuid, player)
    }

    override fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T> {
        val plugin = plugin ?: throw Exception("Plugin already deallocated")

        // Bukkit/Spigot performs Asynchronous units of work via their internal Scheduler
        return AsyncTask<T> { resolve ->
            val runnable = Runnable {
                task { result -> resolve(result) }
            }
            val bukkitTask = plugin.server?.scheduler?.runTaskAsynchronously(plugin, runnable)

            Cancellable {
                bukkitTask?.cancel()
            }
        }
    }

    override fun sync(task: () -> Unit) {
        val plugin = plugin ?: throw Exception("Plugin already deallocated")
        val runnable = Runnable { task() }

        plugin.server?.scheduler?.scheduleSyncDelayedTask(plugin, runnable)
    }

    override val permissions: LuckPerms?
        get() = hooks.permissions

    override val plugin: JavaPlugin?
        get() = pluginRef.get()
}