package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.permissions.PermissionsManager
import com.projectcitybuild.modules.channels.bungeecord.BungeecordMessageListener
import com.projectcitybuild.modules.config.implementations.BungeecordConfig
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.logger.implementations.BungeecordLogger
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
import com.projectcitybuild.old_modules.storage.HubFileStorage
import com.projectcitybuild.platforms.bungeecord.environment.*
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.plugin.Plugin

class BungeecordPlatform: Plugin() {
    private var commandRegistry: BungeecordCommandRegistry? = null
    private var listenerRegistry: BungeecordListenerRegistry? = null
    private var dataSource: DataSource? = null
    private var permissionsManager: PermissionsManager? = null
    private var sessionCache: BungeecordSessionCache? = null
    private var playerConfigCache: PlayerConfigCache? = null

    override fun onEnable() {
        proxy.registerChannel(Channel.BUNGEECORD)

        val component = DaggerBungeecordComponent.builder()
            .plugin(this)
            .proxyServer(proxy)
            .config(BungeecordConfig(dataFolder))
            .logger(BungeecordLogger(logger))
            .apiClient(APIClient { Dispatchers.IO })
            .hubFileStorage(HubFileStorage(dataFolder))
            .build()

        component.config().load(
            PluginConfig.API_KEY,
            PluginConfig.API_BASE_URL,
            PluginConfig.API_IS_LOGGING_ENABLED,
            PluginConfig.WARPS_PER_PAGE,
            PluginConfig.DB_HOSTNAME,
            PluginConfig.DB_PORT,
            PluginConfig.DB_NAME,
            PluginConfig.DB_USERNAME,
            PluginConfig.DB_PASSWORD,
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

        dataSource = component.dataSource().also { it.connect() }
        sessionCache = component.sessionCache()
        permissionsManager = component.permissionsManager()
        playerConfigCache = component.playerConfigCache()

        val logger = component.logger()
        commandRegistry = BungeecordCommandRegistry(plugin = this, logger)
        listenerRegistry = BungeecordListenerRegistry(plugin = this, logger)

        val subChannelListener = BungeecordMessageListener(logger)
        listenerRegistry?.register(subChannelListener)

        component.modules().forEach { module ->
            module.bungeecordCommands.forEach { commandRegistry?.register(it) }
            module.bungeecordListeners.forEach { listenerRegistry?.register(it) }
            module.bungeecordSubChannelListeners.forEach { subChannelListener.register(it) }
        }
    }

    override fun onDisable() {
        proxy.unregisterChannel(Channel.BUNGEECORD)

        playerConfigCache?.flush()
        dataSource?.close()
        listenerRegistry?.unregisterAll()

        permissionsManager = null
        commandRegistry = null
        listenerRegistry = null
        sessionCache = null
        dataSource = null
        playerConfigCache = null
    }
}