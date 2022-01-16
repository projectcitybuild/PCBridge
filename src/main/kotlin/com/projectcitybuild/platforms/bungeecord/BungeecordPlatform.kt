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
            // TODO
//        config.addDefault("groups.appearance.admin.display_name", "§4[Staff]")
//        config.addDefault("groups.appearance.admin.hover_name", "Administrator")
//        config.addDefault("groups.appearance.sop.display_name", "§c[Staff]")
//        config.addDefault("groups.appearance.sop.hover_name", "Senior Operator")
//        config.addDefault("groups.appearance.op.display_name", "§6[Staff]")
//        config.addDefault("groups.appearance.op.hover_name", "Operator")
//        config.addDefault("groups.appearance.moderator.display_name", "§e[Staff]")
//        config.addDefault("groups.appearance.moderator.hover_name", "Moderator")
//
//        config.addDefault("groups.appearance.trusted+.hover_name", "Trusted+")
//        config.addDefault("groups.appearance.trusted.hover_name", "Trusted")
//        config.addDefault("groups.appearance.donator.hover_name", "Donor")
//        config.addDefault("groups.appearance.architect.hover_name", "Architect")
//        config.addDefault("groups.appearance.engineer.hover_name", "Engineer")
//        config.addDefault("groups.appearance.planner.hover_name", "Planner")
//        config.addDefault("groups.appearance.builder.hover_name", "Builder")
//        config.addDefault("groups.appearance.intern.hover_name", "Intern")
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