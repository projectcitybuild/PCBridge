package com.projectcitybuild.plugin

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.core.infrastructure.network.APIClientImpl
import com.projectcitybuild.core.infrastructure.redis.RedisConnection
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.config.implementations.SpigotConfig
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.eventbroadcast.implementations.SpigotLocalEventBroadcaster
import com.projectcitybuild.modules.kick.SpigotPlayerKicker
import com.projectcitybuild.modules.logger.implementations.SpigotLogger
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.modules.scheduler.implementations.SpigotScheduler
import com.projectcitybuild.modules.timer.implementations.SpigotTimer
import com.projectcitybuild.plugin.environment.SpigotCommandRegistry
import com.projectcitybuild.plugin.environment.SpigotListenerRegistry
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

class SpigotPlugin : JavaPlugin() {
    private var container: SpigotPluginContainer? = null

    override fun onEnable() {
        container = DaggerSpigotComponent.builder()
            .plugin(this)
            .javaPlugin(this)
            .server(server)
            .config(SpigotConfig(plugin = this, config))
            .logger(SpigotLogger(logger))
            .scheduler(SpigotScheduler(plugin = this))
            .timer(SpigotTimer(plugin = this))
            .kicker(SpigotPlayerKicker(server))
            .localEventBroadcaster(SpigotLocalEventBroadcaster())
            .apiClient(APIClientImpl { this.minecraftDispatcher })
            .baseFolder(dataFolder)
            .build()
            .container()

        container?.onEnable(server)
    }

    override fun onDisable() {
        container?.onDisable()
        container = null
    }
}

class SpigotPluginContainer @Inject constructor(
    private val container: SpigotContainer,
    private val plugin: Plugin,
    private val config: PlatformConfig,
    private val commandRegistry: SpigotCommandRegistry,
    private val listenerRegistry: SpigotListenerRegistry,
    private val dataSource: DataSource,
    private val errorReporter: ErrorReporter,
    private val redisConnection: RedisConnection,
    private val permissions: Permissions,
) {
    private val isRedisEnabled: Boolean
        get() = config.get(ConfigKey.SHARED_CACHE_ADAPTER) == "redis"

    fun onEnable(server: Server) {
        errorReporter.bootstrap()

        runCatching {
            dataSource.connect()

            if (isRedisEnabled) {
                redisConnection.connect()
            }

            permissions.connect()

            container.integrations.forEach { it.onEnable() }
            container.commands.forEach { commandRegistry.register(it) }
            container.listeners.forEach { listenerRegistry.register(it) }
        }.onFailure {
            reportError(it)
            server.pluginManager.disablePlugin(plugin)
        }
    }

    fun onDisable() {
        runCatching {
            container.integrations.forEach { it.onDisable() }

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
