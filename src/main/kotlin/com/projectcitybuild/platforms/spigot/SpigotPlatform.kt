package com.projectcitybuild.platforms.spigot

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.core.infrastructure.network.APIClientImpl
import com.projectcitybuild.core.infrastructure.redis.RedisConnection
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.channels.spigot.SpigotMessageListener
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.config.implementations.SpigotConfig
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.eventbroadcast.implementations.SpigotLocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.logger.implementations.SpigotLogger
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
        val component = DaggerSpigotComponent.builder()
            .plugin(this)
            .javaPlugin(this)
            .config(SpigotConfig(this, config))
            .logger(SpigotLogger(logger))
            .scheduler(SpigotScheduler(this))
            .localEventBroadcaster(SpigotLocalEventBroadcaster())
            .apiClient(APIClientImpl { this.minecraftDispatcher })
            .baseFolder(dataFolder)
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
        private val config: PlatformConfig,
        private val logger: PlatformLogger,
        private val commandRegistry: SpigotCommandRegistry,
        private val listenerRegistry: SpigotListenerRegistry,
        private val dataSource: DataSource,
        private val errorReporter: ErrorReporter,
        private val redisConnection: RedisConnection,
    ) {
        private val isRedisEnabled: Boolean
            get() = config.get(ConfigKeys.SHARED_CACHE_ADAPTER) == "redis"

        fun onEnable(server: Server) {
            errorReporter.bootstrap()

            runCatching {
                dataSource.connect()

                if (isRedisEnabled) {
                    redisConnection.connect()
                }

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

                if (isRedisEnabled) {
                    redisConnection.disconnect()
                }

            }.onFailure { reportError(it) }
        }

        private fun reportError(throwable: Throwable) {
            throwable.printStackTrace()
            errorReporter.report(throwable)
        }
    }
}
