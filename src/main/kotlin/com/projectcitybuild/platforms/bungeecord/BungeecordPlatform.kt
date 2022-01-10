package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.bans.BanRepository
import com.projectcitybuild.modules.chat.ChatGroupFormatBuilder
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.playerconfig.PlayerConfigFileStorage
import com.projectcitybuild.modules.players.MojangPlayerRepository
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookupService
import com.projectcitybuild.modules.ranks.SyncPlayerGroupService
import com.projectcitybuild.modules.sessioncache.SessionCache
import com.projectcitybuild.modules.storage.HubFileStorage
import com.projectcitybuild.modules.storage.WarpFileStorage
import com.projectcitybuild.platforms.bungeecord.commands.*
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordConfig
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordLogger
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordTimer
import com.projectcitybuild.platforms.bungeecord.listeners.*
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.plugin.Plugin

class BungeecordPlatform: Plugin() {

    private val bungeecordLogger = BungeecordLogger(logger)
    private val config = BungeecordConfig(plugin = this)
    private val apiClient = APIClient { Dispatchers.IO }
    private val timer = BungeecordTimer(plugin = this, proxy)
    private var commandDelegate: BungeecordCommandDelegate? = null
    private var listenerDelegate: BungeecordListenerDelegate? = null
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

    private val playerUUIDLookupService: PlayerUUIDLookupService by lazy {
        PlayerUUIDLookupService(
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
    private var sessionCache: SessionCache? = null

    override fun onEnable() {
        config.load()
        createDefaultConfig()

        proxy.registerChannel(Channel.BUNGEECORD)

        sessionCache = SessionCache()

        permissionsManager = PermissionsManager()

        this.commandDelegate = BungeecordCommandDelegate(plugin = this, logger = bungeecordLogger)
            .also { registerCommands(it) }

        this.listenerDelegate = BungeecordListenerDelegate(plugin = this, logger = bungeecordLogger)
            .also { registerListeners(it) }
    }

    override fun onDisable() {
        proxy.unregisterChannel(Channel.BUNGEECORD)

        listenerDelegate?.unregisterAll()
        timer.cancelAll()

        permissionsManager = null
        commandDelegate = null
        listenerDelegate = null
        sessionCache = null

        playerConfigCache.flush()
    }

    private fun registerCommands(delegate: BungeecordCommandDelegate) {
        arrayOf(
            ACommand(proxy),
            AFKCommand(proxy, sessionCache!!),
            BanCommand(proxy, playerUUIDLookupService, banRepository),
            CheckBanCommand(proxy, playerUUIDLookupService, banRepository),
            DelWarpCommand(warpFileStorage),
            HubCommand(proxy, hubFileStorage),
            IgnoreCommand(proxy, playerUUIDLookupService, playerConfigRepository),
            MuteCommand(proxy, playerConfigRepository),
            SyncCommand(apiRequestFactory, apiClient, syncPlayerGroupService),
            SyncOtherCommand(proxy, syncPlayerGroupService),
            TPCommand(proxy, playerConfigRepository),
            TPOCommand(proxy),
            TPHereCommand(proxy),
            UnbanCommand(proxy, playerUUIDLookupService, banRepository),
            UnignoreCommand(proxy, playerUUIDLookupService, playerConfigRepository),
            UnmuteCommand(proxy, playerConfigRepository),
            WarpCommand(proxy, warpFileStorage),
            WarpsCommand(warpFileStorage),
            WhisperCommand(proxy, playerConfigRepository),
        )
        .forEach { delegate.register(it) }
    }

    private fun registerListeners(delegate: BungeecordListenerDelegate) {
        arrayOf(
            BanConnectionListener(banRepository, bungeecordLogger),
            IncomingAFKEndListener(proxy, sessionCache!!),
            IncomingChatListener(proxy, playerConfigRepository, chatGroupFormatBuilder),
            IncomingSetHubListener(hubFileStorage),
            IncomingSetWarpListener(warpFileStorage),
            SyncRankLoginListener(syncPlayerGroupService),
        )
        .forEach { delegate.register(it) }
    }

    private fun createDefaultConfig() {
        config.addDefaults(
            PluginConfig.API_KEY,
            PluginConfig.API_BASE_URL,
            PluginConfig.API_IS_LOGGING_ENABLED,
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