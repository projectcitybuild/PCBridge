package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.core.infrastructure.network.APIClientImpl
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.integrations.shared.playercache.PlayerConfigCache
import com.projectcitybuild.modules.channels.bungeecord.BungeecordMessageListener
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.config.implementations.BungeecordConfig
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.eventbroadcast.implementations.BungeecordLocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.logger.implementations.BungeecordLogger
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.modules.scheduler.implementations.BungeecordScheduler
import com.projectcitybuild.modules.timer.implementations.BungeecordTimer
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandRegistry
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordListenerRegistry
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import javax.inject.Inject

class BungeecordPlugin : Plugin() {
    private var container: BungeecordPluginContainer? = null

    override fun onEnable() {
        val component = DaggerBungeecordComponent.builder()
            .plugin(this)
            .proxyServer(proxy)
            .config(BungeecordConfig(dataFolder))
            .logger(BungeecordLogger(logger))
            .scheduler(BungeecordScheduler(this))
            .timer(BungeecordTimer(this, proxy))
            .localEventBroadcaster(BungeecordLocalEventBroadcaster(proxy))
            .apiClient(APIClientImpl { Dispatchers.IO })
            .build()

        container = component.container()
        container?.onEnable(component.modules())
    }

    override fun onDisable() {
        container?.onDisable()
        container = null
    }
}

class BungeecordPluginContainer @Inject constructor(
    private val proxyServer: ProxyServer,
    private val logger: PlatformLogger,
    private val config: PlatformConfig,
    private val dataSource: DataSource,
    private val commandRegistry: BungeecordCommandRegistry,
    private val listenerRegistry: BungeecordListenerRegistry,
    private val playerConfigCache: PlayerConfigCache,
    private val errorReporter: ErrorReporter,
    private val permissions: Permissions,
) {
    fun onEnable(modules: List<BungeecordFeatureModule>) {
        errorReporter.bootstrap()

        runCatching {
            proxyServer.registerChannel(Channel.BUNGEECORD)

            dataSource.connect()
            permissions.connect()

            val subChannelListener = BungeecordMessageListener(logger)
            listenerRegistry.register(subChannelListener)

            modules.forEach { module ->
                module.bungeecordCommands.forEach { commandRegistry.register(it) }
                module.bungeecordListeners.forEach { listenerRegistry.register(it) }
                module.bungeecordSubChannelListeners.forEach { subChannelListener.register(it) }
            }

            if (!config.get(ConfigKey.API_ENABLED)) {
                """
                    #********************************************************
                    #
                    #  PCB NETWORK API DISABLED VIA CONFIG
                    #  
                    #  This will prevent the plugin from checking bans and syncing ranks. 
                    #  If this is not a local dev environment, the API should be enabled!
                    #  
                    #********************************************************
                    """
                    .trimMargin("#")
                    .split("\n")
                    .forEach { logger.warning(it) }
            }
        }.onFailure {
            reportError(it)
            proxyServer.pluginManager.getPlugin("PCBridge")?.onDisable()
        }
    }

    fun onDisable() {
        runCatching {
            proxyServer.unregisterChannel(Channel.BUNGEECORD)
            listenerRegistry.unregisterAll()
            dataSource.disconnect()
            playerConfigCache.flush()
        }.onFailure { reportError(it) }
    }

    private fun reportError(throwable: Throwable) {
        throwable.printStackTrace()
        errorReporter.report(throwable)
    }
}
