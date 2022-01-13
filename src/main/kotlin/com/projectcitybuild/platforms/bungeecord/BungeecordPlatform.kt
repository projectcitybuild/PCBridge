package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.modules.network.mojang.client.MojangClient
import com.projectcitybuild.modules.network.pcb.client.PCBClient
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.features.bans.BanModule
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.ranksync.RankSyncModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.features.warps.WarpModule
import com.projectcitybuild.modules.permissions.PermissionsManager
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.chat.ChatGroupFormatBuilder
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigFileStorage
import com.projectcitybuild.modules.playeruuid.MojangPlayerRepository
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.features.ranksync.SyncPlayerGroupService
import com.projectcitybuild.modules.channels.bungeecord.BungeecordMessageListener
import com.projectcitybuild.modules.config.implementations.BungeecordConfig
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.logger.implementations.BungeecordLogger
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordTimer
import com.projectcitybuild.old_modules.storage.HubFileStorage
import com.projectcitybuild.old_modules.storage.WarpFileStorage
import com.projectcitybuild.platforms.bungeecord.environment.*
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.plugin.Plugin

class BungeecordPlatform: Plugin() {

    private val bungeecordLogger = BungeecordLogger(logger)
    private val config = BungeecordConfig(plugin = this)
    private val apiClient = APIClient { Dispatchers.IO }
    private val timer = BungeecordTimer(plugin = this, proxy)
    private var commandRegistry: BungeecordCommandRegistry? = null
    private var listenerRegistry: BungeecordListenerRegistry? = null
    private var permissionsManager: PermissionsManager? = null

    private val apiRequestFactory: APIRequestFactory by lazy {
        val isLoggingEnabled = config.get(PluginConfig.API_IS_LOGGING_ENABLED)
        APIRequestFactory(
            pcb = PCBClient(
                authToken = config.get(PluginConfig.API_KEY),
                baseUrl = config.get(PluginConfig.API_BASE_URL),
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }

    private val dataSource: DataSource by lazy {
        DataSource(
            logger = bungeecordLogger,
            hostName = config.get(PluginConfig.DB_HOSTNAME),
            port = config.get(PluginConfig.DB_PORT),
            databaseName = config.get(PluginConfig.DB_NAME),
            databaseUsername = config.get(PluginConfig.DB_USERNAME),
            databasePassword = config.get(PluginConfig.DB_PASSWORD)
        )
    }

    private val playerUUIDRepository: PlayerUUIDRepository by lazy {
        PlayerUUIDRepository(
            proxy,
            mojangPlayerRepository
        )
    }

    private val mojangPlayerRepository: MojangPlayerRepository by lazy {
        MojangPlayerRepository(
            apiRequestFactory,
            apiClient
        )
    }

    private val playerConfigRepository: PlayerConfigRepository by lazy {
        PlayerConfigRepository(
            playerConfigCache,
            PlayerConfigFileStorage(
                folderPath = dataFolder.resolve("players")
            )
        )
    }

    private val warpFileStorage: WarpFileStorage by lazy {
        WarpFileStorage(
            folderPath = dataFolder.resolve("warps")
        )
    }

    private val hubFileStorage: HubFileStorage by lazy {
        HubFileStorage(
            folderPath = dataFolder
        )
    }

    private val banRepository: BanRepository by lazy {
        BanRepository(
            apiRequestFactory,
            apiClient
        )
    }

    private val syncPlayerGroupService: SyncPlayerGroupService by lazy {
        SyncPlayerGroupService(
            permissionsManager!!,
            apiRequestFactory,
            apiClient,
            config,
            bungeecordLogger
        )
    }

    private val chatGroupFormatBuilder: ChatGroupFormatBuilder by lazy {
        ChatGroupFormatBuilder(
            permissionsManager!!,
            config
        )
    }

    private val playerConfigCache = PlayerConfigCache()
    private var sessionCache: BungeecordSessionCache? = null

    override fun onEnable() {
        config.load()
        createDefaultConfig()

        dataSource.connect()

        proxy.registerChannel(Channel.BUNGEECORD)

        sessionCache = BungeecordSessionCache()
        permissionsManager = PermissionsManager()
        commandRegistry = BungeecordCommandRegistry(plugin = this, bungeecordLogger)
        listenerRegistry = BungeecordListenerRegistry(plugin = this, bungeecordLogger)

        val subChannelListener = BungeecordMessageListener(bungeecordLogger)
        listenerRegistry?.register(subChannelListener)

        arrayOf(
            BanModule(
                plugin = this,
                proxy,
                playerUUIDRepository,
                banRepository,
                bungeecordLogger
            ),
            ChatModule.Bungeecord(
                proxy,
                playerUUIDRepository,
                playerConfigRepository,
                chatGroupFormatBuilder,
                sessionCache!!,
                NameGuesser()
            ),
            HubModule.Bungeecord(
                proxy,
                hubFileStorage
            ),
            JoinMessageModule.Bungee(
                proxy
            ),
            RankSyncModule(
                proxy,
                apiRequestFactory,
                apiClient,
                syncPlayerGroupService,
                NameGuesser()
            ),
            TeleportModule.Bungeecord(
                proxy,
                playerConfigRepository,
                NameGuesser()
            ),
            WarpModule.Bungeecord(
                proxy,
                warpFileStorage,
                NameGuesser(),
                config
            ),
        ).forEach { module ->
            module.bungeecordCommands.forEach { commandRegistry?.register(it) }
            module.bungeecordListeners.forEach { listenerRegistry?.register(it) }
            module.bungeecordSubChannelListeners.forEach { subChannelListener.register(it) }
        }
    }

    override fun onDisable() {
        proxy.unregisterChannel(Channel.BUNGEECORD)

        listenerRegistry?.unregisterAll()
        timer.cancelAll()

        permissionsManager = null
        commandRegistry = null
        listenerRegistry = null
        sessionCache = null

        playerConfigCache.flush()

        dataSource.close()
    }

    private fun createDefaultConfig() {
        config.addDefaults(
            PluginConfig.API_KEY,
            PluginConfig.API_BASE_URL,
            PluginConfig.API_IS_LOGGING_ENABLED,
            PluginConfig.WARPS_PER_PAGE,
            PluginConfig.DB_HOSTNAME,
            PluginConfig.DB_PORT,
            PluginConfig.DB_NAME,
            PluginConfig.DB_USERNAME,
            PluginConfig.DB_PASSWORD,
        )

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
    }
}