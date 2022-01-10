package com.projectcitybuild.platforms.spigot

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.modules.network.mojang.client.MojangClient
import com.projectcitybuild.modules.network.pcb.client.PCBClient
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.warps.WarpModule
import com.projectcitybuild.modules.sessioncache.SpigotSessionCache
import com.projectcitybuild.platforms.spigot.environment.*
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.modules.channels.spigot.SpigotMessageListener
import com.projectcitybuild.modules.config.implementations.SpigotConfig
import com.projectcitybuild.modules.logger.implementations.SpigotLogger
import com.projectcitybuild.modules.permissions.PermissionsManager
import com.projectcitybuild.modules.scheduler.implementations.SpigotScheduler
import com.projectcitybuild.platforms.spigot.listeners.PendingJoinActionListener
import org.bukkit.plugin.java.JavaPlugin

class SpigotPlatform: JavaPlugin() {

    private val spigotLogger = SpigotLogger(logger = logger)
    private val spigotConfig = SpigotConfig(plugin = this, config = config)
    private val scheduler = SpigotScheduler(plugin = this)
    private val apiClient = APIClient(getCoroutineContext = {
        // To prevent Coroutines being created before the plugin is ready
        this.minecraftDispatcher
    })
    private var permissionsManager: PermissionsManager? = null
    private var commandRegistry: SpigotCommandRegistry? = null
    private var listenerRegistry: SpigotListenerRegistry? = null

    private val apiRequestFactory: APIRequestFactory by lazy {
        val isLoggingEnabled = spigotConfig.get(PluginConfig.API_IS_LOGGING_ENABLED)

        APIRequestFactory(
            pcb = PCBClient(
                authToken = spigotConfig.get(PluginConfig.API_KEY),
                baseUrl = spigotConfig.get(PluginConfig.API_BASE_URL),
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }

    private var spigotSessionCache: SpigotSessionCache? = null

    override fun onEnable() {
        createDefaultConfig()

        spigotSessionCache = SpigotSessionCache()

        val pluginMessageListener = SpigotMessageListener(spigotLogger)

        server.messenger.registerOutgoingPluginChannel(this, Channel.BUNGEECORD)
        server.messenger.registerIncomingPluginChannel(this, Channel.BUNGEECORD, pluginMessageListener)

        permissionsManager = PermissionsManager()

        commandRegistry = SpigotCommandRegistry(plugin = this, spigotLogger)
        listenerRegistry = SpigotListenerRegistry(plugin = this, spigotLogger)

        arrayOf(
            ChatModule.Spigot(plugin = this),
            HubModule.Spigot(plugin = this),
            JoinMessageModule.Spigot(),
            TeleportModule.Spigot(plugin = this, spigotLogger, spigotSessionCache!!),
            WarpModule.Spigot(plugin = this, spigotLogger, spigotSessionCache!!),
        )
        .forEach { module ->
            module.spigotCommands.forEach { commandRegistry?.register(it) }
            module.spigotListeners.forEach { listenerRegistry?.register(it) }
            module.spigotSubChannelListeners.forEach { pluginMessageListener.register(it) }
        }

        listenerRegistry?.register(
            PendingJoinActionListener(spigotSessionCache!!, spigotLogger)
        )

        logger.info("PCBridge ready")
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(this)
        server.messenger.unregisterIncomingPluginChannel(this)

        listenerRegistry?.unregisterAll()

        spigotSessionCache = null
        commandRegistry = null
        listenerRegistry = null
        permissionsManager = null

        logger.info("PCBridge disabled")
    }

    private fun createDefaultConfig() {
        spigotConfig.addDefaults(
            PluginConfig.API_KEY,
            PluginConfig.API_BASE_URL,
            PluginConfig.API_IS_LOGGING_ENABLED,
        )
    }
}
