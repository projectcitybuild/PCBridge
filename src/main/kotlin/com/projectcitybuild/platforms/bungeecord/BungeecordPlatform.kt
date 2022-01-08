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
import com.projectcitybuild.platforms.bungeecord.commands.*
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordConfig
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordLogger
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordTimer
import com.projectcitybuild.platforms.bungeecord.listeners.BanConnectionListener
import com.projectcitybuild.platforms.bungeecord.listeners.IncomingAFKEndListener
import com.projectcitybuild.platforms.bungeecord.listeners.IncomingChatListener
import com.projectcitybuild.platforms.bungeecord.listeners.SyncRankLoginListener
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

        val commandDelegate = BungeecordCommandDelegate(plugin = this, logger = bungeecordLogger)
        registerCommands(commandDelegate)
        this.commandDelegate = commandDelegate

        val listenerDelegate = BungeecordListenerDelegate(plugin = this, logger = bungeecordLogger)
        registerListeners(listenerDelegate)
        this.listenerDelegate = listenerDelegate
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
            BanCommand(proxy, playerUUIDLookupService, banRepository),
            UnbanCommand(proxy, playerUUIDLookupService, banRepository),
            CheckBanCommand(playerUUIDLookupService, banRepository),
            SyncCommand(apiRequestFactory, apiClient, syncPlayerGroupService),
            SyncOtherCommand(proxy, syncPlayerGroupService),
            MuteCommand(proxy, playerConfigRepository),
            UnmuteCommand(proxy, playerConfigRepository),
            IgnoreCommand(playerUUIDLookupService, playerConfigRepository),
            UnignoreCommand(playerUUIDLookupService, playerConfigRepository),
            WhisperCommand(proxy),
            ACommand(proxy),
            AFKCommand(proxy, sessionCache!!),
        )
        .forEach { delegate.register(it) }
    }

    private fun registerListeners(delegate: BungeecordListenerDelegate) {
        arrayOf(
            BanConnectionListener(banRepository, bungeecordLogger),
            SyncRankLoginListener(syncPlayerGroupService),
            IncomingChatListener(proxy, playerConfigRepository, chatGroupFormatBuilder),
            IncomingAFKEndListener(proxy, sessionCache!!)
        )
        .forEach { delegate.register(it) }
    }

    private fun createDefaultConfig() {
        config.addDefaults(
            PluginConfig.API_KEY,
            PluginConfig.API_BASE_URL,
            PluginConfig.API_IS_LOGGING_ENABLED,
        )
    }
}