package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.bans.BanRepository
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.players.MojangPlayerRepository
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.modules.storage.implementations.PlayerConfigFileStorage
import com.projectcitybuild.platforms.bungeecord.commands.*
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordConfig
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordLogger
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordTimer
import com.projectcitybuild.platforms.bungeecord.listeners.BanConnectionListener
import com.projectcitybuild.platforms.bungeecord.listeners.IncomingChatListener
import com.projectcitybuild.platforms.bungeecord.listeners.IncomingStaffChatListener
import com.projectcitybuild.platforms.bungeecord.listeners.SyncRankLoginListener
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File

class BungeecordPlatform: Plugin() {

    private val bungeecordLogger = BungeecordLogger(logger)
    private val config = BungeecordConfig(plugin = this)
    private val apiClient = APIClient { Dispatchers.IO }
    private val timer = BungeecordTimer(this, proxy)
    private var commandDelegate: BungeecordCommandDelegate? = null
    private var listenerDelegate: BungeecordListenerDelegate? = null
    private var permissionsManager: PermissionsManager? = null

    private val apiRequestFactory: APIRequestFactory by lazy {
        val isLoggingEnabled = config.get(PluginConfig.API.IS_LOGGING_ENABLED)
        APIRequestFactory(
            pcb = PCBClient(
                authToken = config.get(PluginConfig.API.KEY),
                baseUrl = config.get(PluginConfig.API.BASE_URL),
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }

    private val playerUUIDLookup: PlayerUUIDLookup by lazy {
        PlayerUUIDLookup(
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
                folderPath = dataFolder.resolve("players"),
            )
        )
    }

    private val banRepository: BanRepository by lazy {
        BanRepository(
            apiRequestFactory,
            apiClient
        )
    }

    private val syncPlayerGroupAction: SyncPlayerGroupAction by lazy {
        SyncPlayerGroupAction(
            permissionsManager!!,
            apiRequestFactory,
            apiClient,
            config,
            bungeecordLogger
        )
    }

    private val playerConfigCache = PlayerConfigCache()

    override fun onEnable() {
        createDefaultConfig()

        proxy.registerChannel(Channel.BUNGEECORD)

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

        playerConfigCache.flush()
    }

    private fun registerCommands(delegate: BungeecordCommandDelegate) {
        arrayOf(
            BanCommand(proxy, playerUUIDLookup, banRepository),
            UnbanCommand(proxy, playerUUIDLookup, banRepository),
            CheckBanCommand(playerUUIDLookup, banRepository),
            SyncCommand(apiRequestFactory, apiClient, syncPlayerGroupAction),
            SyncOtherCommand(proxy, syncPlayerGroupAction),
            MuteCommand(proxy, playerConfigRepository),
            UnmuteCommand(proxy, playerConfigRepository),
        )
        .forEach { delegate.register(it) }
    }

    private fun registerListeners(delegate: BungeecordListenerDelegate) {
        arrayOf(
            BanConnectionListener(banRepository, bungeecordLogger),
            SyncRankLoginListener(syncPlayerGroupAction),
            IncomingChatListener(proxy, playerConfigRepository),
            IncomingStaffChatListener(proxy)
        )
        .forEach { delegate.register(it) }
    }

    private fun createDefaultConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        val file = File(dataFolder, "config.yml")

        if (!file.exists()) {
            // FIXME
//            try {
//                getResourceAsStream("config.yml").use { `in` ->
//                    val out = FileOutputStream(file)
//                    ByteStreams.copy(in, out)
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
        }

        if (!file.exists()) {
            val configProvider = ConfigurationProvider
                    .getProvider(YamlConfiguration::class.java)

            val config = configProvider.load(file)

            config.set(PluginConfig.API.KEY.key, "")
            config.set(PluginConfig.API.BASE_URL.key, "")
            config.set(PluginConfig.API.IS_LOGGING_ENABLED.key, false)

            configProvider.save(config, file)
        }
    }
}