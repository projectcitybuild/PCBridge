package com.projectcitybuild.platforms.spigot

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.channels.spigot.SpigotMessageListener
import com.projectcitybuild.modules.config.implementations.SpigotConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.logger.implementations.SpigotLogger
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandRegistry
import com.projectcitybuild.platforms.spigot.environment.SpigotListenerRegistry
import com.projectcitybuild.platforms.spigot.listeners.PendingJoinActionListener
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

class SpigotPlatform: JavaPlugin() {
    private lateinit var container: Container

    override fun onEnable() {
        val config = SpigotConfig(plugin = this, config = config).apply {
            load(
                PluginConfig.API_KEY,
                PluginConfig.API_BASE_URL,
                PluginConfig.API_IS_LOGGING_ENABLED,
            )
        }

        val component = DaggerSpigotComponent.builder()
            .plugin(this)
            .javaPlugin(this)
            .config(config)
            .logger(SpigotLogger(logger = logger))
            .apiClient(APIClient { this.minecraftDispatcher })
            .build()

        container = component.container()
        container.onEnable(server, component.modules())
    }

    override fun onDisable() {
        container.onDisable(server)
    }

    class Container @Inject constructor(
        private val plugin: Plugin,
        private val logger: PlatformLogger,
        private val commandRegistry: SpigotCommandRegistry,
        private val listenerRegistry: SpigotListenerRegistry,
        private val pendingJoinActionListener: PendingJoinActionListener,
    ) {
        fun onEnable(server: Server, modules: List<SpigotFeatureModule>) {
            val pluginMessageListener = SpigotMessageListener(logger)
            server.messenger.registerOutgoingPluginChannel(plugin, Channel.BUNGEECORD)
            server.messenger.registerIncomingPluginChannel(plugin, Channel.BUNGEECORD, pluginMessageListener)

            listenerRegistry.register(pendingJoinActionListener)

            modules.forEach { module ->
                module.spigotCommands.forEach { commandRegistry.register(it) }
                module.spigotListeners.forEach { listenerRegistry.register(it) }
                module.spigotSubChannelListeners.forEach { pluginMessageListener.register(it) }
            }
        }

        fun onDisable(server: Server) {
            server.messenger.unregisterOutgoingPluginChannel(plugin)
            server.messenger.unregisterIncomingPluginChannel(plugin)

            listenerRegistry.unregisterAll()
        }
    }
}
