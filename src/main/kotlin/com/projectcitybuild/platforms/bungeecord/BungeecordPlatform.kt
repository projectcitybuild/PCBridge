package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.channels.bungeecord.BungeecordMessageListener
import com.projectcitybuild.modules.config.implementations.BungeecordConfig
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.logger.implementations.BungeecordLogger
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
import com.projectcitybuild.features.hub.storage.HubFileStorage
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandRegistry
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordListenerRegistry
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import javax.inject.Inject

class BungeecordPlatform: Plugin() {
    private lateinit var container: Container

    override fun onEnable() {
        val config = BungeecordConfig(dataFolder).apply {
            load(
                PluginConfig.API_KEY,
                PluginConfig.API_BASE_URL,
                PluginConfig.API_IS_LOGGING_ENABLED,
                PluginConfig.WARPS_PER_PAGE,
                PluginConfig.DB_HOSTNAME,
                PluginConfig.DB_PORT,
                PluginConfig.DB_NAME,
                PluginConfig.DB_USERNAME,
                PluginConfig.DB_PASSWORD,
                PluginConfig.ERROR_REPORTING_SENTRY_ENABLED,
                PluginConfig.ERROR_REPORTING_SENTRY_DSN,
                PluginConfig.GROUPS_APPEARANCE_ADMIN_DISPLAY_NAME,
                PluginConfig.GROUPS_APPEARANCE_ADMIN_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_SOP_DISPLAY_NAME,
                PluginConfig.GROUPS_APPEARANCE_SOP_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_OP_DISPLAY_NAME,
                PluginConfig.GROUPS_APPEARANCE_OP_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_MODERATOR_DISPLAY_NAME,
                PluginConfig.GROUPS_APPEARANCE_MODERATOR_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_TRUSTEDPLUS_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_TRUSTED_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_DONOR_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_ARCHITECT_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_ENGINEER_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_PLANNER_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_BUILDER_HOVER_NAME,
                PluginConfig.GROUPS_APPEARANCE_INTERN_HOVER_NAME,
            )
        }

        val component = DaggerBungeecordComponent.builder()
            .plugin(this)
            .proxyServer(proxy)
            .config(config)
            .logger(BungeecordLogger(logger))
            .apiClient(APIClient { Dispatchers.IO })
            .hubFileStorage(HubFileStorage(dataFolder))
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
        private val sessionCache: BungeecordSessionCache,
        private val commandRegistry: BungeecordCommandRegistry,
        private val listenerRegistry: BungeecordListenerRegistry,
        private val playerConfigCache: PlayerConfigCache,
        private val errorReporter: ErrorReporter,
    ) {
        fun onEnable(modules: List<BungeecordFeatureModule>) {
            errorReporter.bootstrap()

            proxyServer.registerChannel(Channel.BUNGEECORD)

            dataSource.connect()

            val subChannelListener = BungeecordMessageListener(logger)
            listenerRegistry.register(subChannelListener)

            modules.forEach { module ->
                module.bungeecordCommands.forEach { commandRegistry.register(it) }
                module.bungeecordListeners.forEach { listenerRegistry.register(it) }
                module.bungeecordSubChannelListeners.forEach { subChannelListener.register(it) }
            }
        }

        fun onDisable() {
            proxyServer.unregisterChannel(Channel.BUNGEECORD)

            dataSource.disconnect()
            sessionCache.flush()
            playerConfigCache.flush()
            listenerRegistry.unregisterAll()
        }
    }
}