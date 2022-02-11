package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.channels.bungeecord.BungeecordMessageListener
import com.projectcitybuild.modules.config.implementations.BungeecordConfig
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.logger.implementations.BungeecordLogger
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIClientImpl
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.scheduler.implementations.BungeecordScheduler
import com.projectcitybuild.modules.timer.implementations.BungeecordTimer
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandRegistry
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordListenerRegistry
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import javax.inject.Inject

class BungeecordPlatform: Plugin() {
    private lateinit var container: Container

    override fun onEnable() {
        val component = DaggerBungeecordComponent.builder()
            .plugin(this)
            .proxyServer(proxy)
            .config(BungeecordConfig(dataFolder))
            .logger(BungeecordLogger(logger))
            .scheduler(BungeecordScheduler(this))
            .timer(BungeecordTimer(this, proxy))
            .apiClient(APIClientImpl { Dispatchers.IO })
            .build()

        container = component.container()
        container.onEnable(component.modules())
    }

    override fun onDisable() {
        container.onDisable()
    }

    class Container @Inject constructor(
        private val proxyServer: ProxyServer,
        private val logger: PlatformLogger,
        private val dataSource: DataSource,
        private val commandRegistry: BungeecordCommandRegistry,
        private val listenerRegistry: BungeecordListenerRegistry,
        private val playerConfigCache: PlayerConfigCache,
        private val errorReporter: ErrorReporter,
    ) {
        fun onEnable(modules: List<BungeecordFeatureModule>) {
            errorReporter.bootstrap()

            runCatching {
                proxyServer.registerChannel(Channel.BUNGEECORD)

                dataSource.connect()

                val subChannelListener = BungeecordMessageListener(logger)
                listenerRegistry.register(subChannelListener)

                modules.forEach { module ->
                    module.bungeecordCommands.forEach { commandRegistry.register(it) }
                    module.bungeecordListeners.forEach { listenerRegistry.register(it) }
                    module.bungeecordSubChannelListeners.forEach { subChannelListener.register(it) }
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
}