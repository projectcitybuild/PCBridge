package com.projectcitybuild.platforms.spigot

import com.github.shynixn.mccoroutine.minecraftDispatcher
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
import com.projectcitybuild.modules.redis.RedisConnection
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
                PluginConfig.REDIS_HOSTNAME,
                PluginConfig.REDIS_PORT,
                PluginConfig.REDIS_USERNAME,
                PluginConfig.REDIS_PASSWORD,
                PluginConfig.ERROR_REPORTING_SENTRY_ENABLED,
                PluginConfig.ERROR_REPORTING_SENTRY_DSN,
                PluginConfig.INTEGRATION_DYNMAP_WARP_ICON,
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
        container.onEnable(server)
    }

    override fun onDisable() {
        container.onDisable(server)
    }

    class Container @Inject constructor(
        private val modulesContainer: SpigotModulesContainer,
        private val plugin: Plugin,
        private val logger: PlatformLogger,
        private val commandRegistry: SpigotCommandRegistry,
        private val listenerRegistry: SpigotListenerRegistry,
        private val dataSource: DataSource,
        private val errorReporter: ErrorReporter,
        private val redisConnection: RedisConnection,
    ) {
        fun onEnable(server: Server) {
            errorReporter.bootstrap()

            runCatching {
                redisConnection.connect()
                dataSource.connect()

                val pluginMessageListener = SpigotMessageListener(logger)
                server.messenger.registerOutgoingPluginChannel(plugin, Channel.BUNGEECORD)
                server.messenger.registerIncomingPluginChannel(plugin, Channel.BUNGEECORD, pluginMessageListener)

                modulesContainer.modules.forEach { module ->
                    logger.verbose("Registering ${module::class.java.name} module")

                    module.spigotCommands.forEach { commandRegistry.register(it) }
                    module.spigotListeners.forEach { listenerRegistry.register(it) }
                    module.spigotSubChannelListeners.forEach { pluginMessageListener.register(it) }
                    module.onEnable()
                }

            }.onFailure {
                reportError(it)
                server.pluginManager.disablePlugin(plugin)
            }
        }

        fun onDisable(server: Server) {
            runCatching {
                modulesContainer.modules.forEach { it.onDisable() }

                server.messenger.unregisterOutgoingPluginChannel(plugin)
                server.messenger.unregisterIncomingPluginChannel(plugin)

                listenerRegistry.unregisterAll()

                dataSource.disconnect()
                redisConnection.disconnect()

            }.onFailure { reportError(it) }
        }

        private fun reportError(throwable: Throwable) {
            throwable.printStackTrace()
            errorReporter.report(throwable)
        }
    }
}
