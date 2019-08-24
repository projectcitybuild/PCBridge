package com.projectcitybuild.spigot.environment

import com.projectcitybuild.api.client.MojangClient
import com.projectcitybuild.api.client.PCBClient
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.services.PlayerStore
import com.projectcitybuild.entities.AsyncCancellable
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.Result
import com.projectcitybuild.entities.models.Player
import com.projectcitybuild.entities.models.PluginConfig
import com.projectcitybuild.entities.models.PluginConfigPair
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.permission.Permission
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
        private val hooks: SpigotPluginHook
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

    override fun <T> async(task: ((Result<T>) -> Unit) -> Void): AsyncCancellable<T> {
        val plugin = plugin ?: throw Exception("Plugin already deallocated")
        val cancellable = AsyncCancellable<T>()

        val runnable = Runnable {
            task { result ->
                cancellable.resolve(result)
            }
        }
        val bukkitTask = plugin.server?.scheduler?.runTaskAsynchronously(plugin, runnable)
        cancellable.cancelListener = {
            bukkitTask?.cancel()
        }
        return cancellable
    }


    override val permissions: Permission? = hooks.permissions
    override val chat: Chat? = hooks.chat

    private var pcbClient: PCBClient? = null
    override val apiClient: PCBClient
        get() {
            if (pcbClient == null) {
                val authToken = get(PluginConfig.Api.KEY()) as? String
                        ?: throw Exception("Could not cast auth token to String")

                val baseUrl = get(PluginConfig.Api.BASE_URL()) as? String
                        ?: throw Exception("Could not cast base url to String")

                pcbClient = PCBClient(authToken = authToken, baseUrl = baseUrl)
            }
            return pcbClient!!
        }

    override val mojangClient: MojangClient = MojangClient()

    override val plugin: JavaPlugin?
        get() = pluginRef.get()
}