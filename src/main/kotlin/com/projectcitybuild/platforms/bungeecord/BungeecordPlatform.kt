package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.modules.network.mojang.client.MojangClient
import com.projectcitybuild.modules.network.pcb.client.PCBClient
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.permissions.PermissionsManager
import com.projectcitybuild.modules.channels.bungeecord.BungeecordMessageListener
import com.projectcitybuild.modules.config.implementations.BungeecordConfig
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.logger.implementations.BungeecordLogger
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
import com.projectcitybuild.platforms.bungeecord.environment.*
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.plugin.Plugin
import javax.inject.Inject

class BungeecordPlatform: Plugin() {
    private var commandRegistry: BungeecordCommandRegistry? = null
    private var listenerRegistry: BungeecordListenerRegistry? = null

//    private val hubFileStorage: HubFileStorage by lazy {
//        HubFileStorage(
//            folderPath = dataFolder
//        )
//    }

    override fun onEnable() {
        val component = DaggerBungeecordComponent.builder()
            .proxyServer(proxy)
            .config(BungeecordConfig(dataFolder))
            .logger(BungeecordLogger(logger))
            .apiClient(APIClient { Dispatchers.IO })
            .apiRequestFactory(APIRequestFactory(
                pcb = PCBClient(
                    authToken = config.get(PluginConfig.API_KEY),
                    baseUrl = config.get(PluginConfig.API_BASE_URL),
                    withLogging = config.get(PluginConfig.API_IS_LOGGING_ENABLED)
                ),
                mojang = MojangClient(
                    withLogging = config.get(PluginConfig.API_IS_LOGGING_ENABLED)
                )
            ))
            .dataSource(DataSource(
                this,
                logger = bungeecordLogger,
                hostName = config.get(PluginConfig.DB_HOSTNAME),
                port = config.get(PluginConfig.DB_PORT),
                databaseName = config.get(PluginConfig.DB_NAME),
                username = config.get(PluginConfig.DB_USERNAME),
                password = config.get(PluginConfig.DB_PASSWORD),
                shouldRunMigrations = true
            ))
            .build()

        val config = component.config()



        config.load(
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

        dataSource.connect()

        proxy.registerChannel(Channel.BUNGEECORD)

        sessionCache = BungeecordSessionCache()
        permissionsManager = PermissionsManager()
        commandRegistry = BungeecordCommandRegistry(plugin = this, component.logger())
        listenerRegistry = BungeecordListenerRegistry(plugin = this, component.logger())

        val subChannelListener = BungeecordMessageListener(component.logger())
        listenerRegistry?.register(subChannelListener)

        arrayOf(
            component.banModule(),
            component.chatModule(),
            component.hubModule(),
            component.joinMessageModule(),
            component.playerCacheModule(),
            component.rankSyncModule(),
            component.teleportModule(),
            component.warpModule(),
        ).forEach { module ->
            module.bungeecordCommands.forEach { commandRegistry?.register(it) }
            module.bungeecordListeners.forEach { listenerRegistry?.register(it) }
            module.bungeecordSubChannelListeners.forEach { subChannelListener.register(it) }
        }
    }

    override fun onDisable() {
        proxy.unregisterChannel(Channel.BUNGEECORD)

        listenerRegistry?.unregisterAll()

        permissionsManager = null
        commandRegistry = null
        listenerRegistry = null
        sessionCache = null

        playerConfigCache.flush()

        dataSource.close()
    }
}