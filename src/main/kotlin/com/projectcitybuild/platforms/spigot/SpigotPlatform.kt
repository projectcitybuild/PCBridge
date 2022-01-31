package com.projectcitybuild.platforms.spigot

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.channels.spigot.SpigotMessageListener
import com.projectcitybuild.modules.config.implementations.SpigotConfig
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.eventbroadcast.SpigotLocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.logger.implementations.SpigotLogger
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.scheduler.implementations.SpigotScheduler
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandRegistry
import com.projectcitybuild.platforms.spigot.environment.SpigotListenerRegistry
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

class SpigotPlatform: JavaPlugin() {
    private lateinit var container: Container

    override fun onEnable() {
        val config = SpigotConfig(plugin = this, config = config).apply {
            load(
                PluginConfig.SPIGOT_SERVER_NAME,
                PluginConfig.DB_HOSTNAME,
                PluginConfig.DB_PORT,
                PluginConfig.DB_NAME,
                PluginConfig.DB_USERNAME,
                PluginConfig.DB_PASSWORD,
                PluginConfig.ERROR_REPORTING_SENTRY_ENABLED,
                PluginConfig.ERROR_REPORTING_SENTRY_DSN,
            )
        }

        val component = DaggerSpigotComponent.builder()
            .plugin(this)
            .javaPlugin(this)
            .config(config)
            .logger(SpigotLogger(logger = logger))
            .scheduler(SpigotScheduler(this))
            .localEventBroadcaster(SpigotLocalEventBroadcaster())
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
        private val dataSource: DataSource,
        private val errorReporter: ErrorReporter,
    ) {
        fun onEnable(server: Server, modules: List<SpigotFeatureModule>) {
            errorReporter.bootstrap()

            dataSource.connect()

            val pluginMessageListener = SpigotMessageListener(logger)
            server.messenger.registerOutgoingPluginChannel(plugin, Channel.BUNGEECORD)
            server.messenger.registerIncomingPluginChannel(plugin, Channel.BUNGEECORD, pluginMessageListener)

            modules.forEach { module ->
                logger.verbose("Registering ${module::class.java.name} module")

                module.spigotCommands.forEach { commandRegistry.register(it) }
                module.spigotListeners.forEach { listenerRegistry.register(it) }
                module.spigotSubChannelListeners.forEach { pluginMessageListener.register(it) }
            }
        }

        fun onDisable(server: Server) {
            server.messenger.unregisterOutgoingPluginChannel(plugin)
            server.messenger.unregisterIncomingPluginChannel(plugin)

            listenerRegistry.unregisterAll()

            dataSource.close()
        }
    }
}
