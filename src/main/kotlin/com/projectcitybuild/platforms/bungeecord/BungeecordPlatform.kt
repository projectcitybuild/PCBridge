package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.entities.PluginConfig
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.modules.bans.CreateUnbanAction
import com.projectcitybuild.modules.players.GetMojangPlayerAction
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.bungeecord.commands.BanCommand
import com.projectcitybuild.platforms.bungeecord.commands.CheckBanCommand
import com.projectcitybuild.platforms.bungeecord.commands.UnbanCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordConfig
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordLogger
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordTimer
import com.projectcitybuild.platforms.bungeecord.listeners.BanConnectionListener
import com.projectcitybuild.platforms.bungeecord.listeners.SyncRankLoginListener
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import kotlinx.coroutines.Dispatchers
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File

class BungeecordPlatform: Plugin() {

    private val bungeecordLogger = BungeecordLogger(logger = this.logger)
    private val config = BungeecordConfig(plugin = this)
    private val apiClient = APIClient(getCoroutineContext = {
        Dispatchers.IO
    })
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

    override fun onEnable() {
        createDefaultConfig()

        permissionsManager = PermissionsManager()

        val commandDelegate = BungeecordCommandDelegate(plugin = this, logger = bungeecordLogger)
        registerCommands(commandDelegate)
        this.commandDelegate = commandDelegate

        val listenerDelegate = BungeecordListenerDelegate(plugin = this, logger = bungeecordLogger)
        registerListeners(listenerDelegate)
        this.listenerDelegate = listenerDelegate
    }

    override fun onDisable() {
        listenerDelegate?.unregisterAll()
        timer.cancelAll()

        permissionsManager = null
        commandDelegate = null
        listenerDelegate = null
    }

    private fun registerCommands(delegate: BungeecordCommandDelegate) {
        arrayOf(
            BanCommand(
                proxyServer = proxy,
                playerUUIDLookup = PlayerUUIDLookup(
                    proxy,
                    GetMojangPlayerAction(
                        apiRequestFactory,
                        apiClient
                    )
                ),
                createBanAction = CreateBanAction(
                    apiRequestFactory,
                    apiClient
                )
            ),
            UnbanCommand(
                proxyServer = proxy,
                playerUUIDLookup = PlayerUUIDLookup(
                    proxy,
                    GetMojangPlayerAction(
                        apiRequestFactory,
                        apiClient
                    )
                ),
                unbanAction = CreateUnbanAction(
                    apiRequestFactory,
                    apiClient
                )
            ),
            CheckBanCommand(
                playerUUIDLookup = PlayerUUIDLookup(
                    proxyServer = proxy,
                    getMojangPlayerAction = GetMojangPlayerAction(
                        apiRequestFactory,
                        apiClient
                    )
                ),
                checkBanStatusAction = CheckBanStatusAction(
                    apiRequestFactory,
                    apiClient
                )
            ),
        )
        .forEach { command -> delegate.register(command) }
    }

    private fun registerListeners(delegate: BungeecordListenerDelegate) {
        arrayOf(
            BanConnectionListener(
                checkBanStatusAction = CheckBanStatusAction(
                    apiRequestFactory = apiRequestFactory,
                    apiClient = apiClient
                )
            ),
            SyncRankLoginListener(
                syncPlayerGroupAction = SyncPlayerGroupAction(
                    permissionsManager = permissionsManager!!,
                    apiRequestFactory = apiRequestFactory,
                    apiClient = apiClient,
                    config = config,
                    logger = bungeecordLogger
                )
            ),
        )
        .forEach { listener -> delegate.register(listener) }
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

            config.set(PluginConfig.SETTINGS.MAINTENANCE_MODE.key, false)
            config.set(PluginConfig.API.KEY.key, "")
            config.set(PluginConfig.API.BASE_URL.key, "")
            config.set(PluginConfig.API.IS_LOGGING_ENABLED.key, false)

            configProvider.save(config, file)
        }
    }
}