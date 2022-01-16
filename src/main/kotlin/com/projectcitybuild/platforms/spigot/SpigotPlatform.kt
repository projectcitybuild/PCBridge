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
import kotlin.math.log

class SpigotPlatform: JavaPlugin() {

    private var commandRegistry: SpigotCommandRegistry? = null
    private var listenerRegistry: SpigotListenerRegistry? = null
    private var permissionsManager: PermissionsManager? = null
    private var spigotSessionCache: SpigotSessionCache? = null

    override fun onEnable() {
        val component = DaggerSpigotComponent.builder()
            .plugin(this)
            .config(SpigotConfig(plugin = this, config = config))
            .logger(SpigotLogger(logger = logger))
            .apiClient(APIClient { this.minecraftDispatcher })
            .build()

        component.config().load(
            PluginConfig.API_KEY,
            PluginConfig.API_BASE_URL,
            PluginConfig.API_IS_LOGGING_ENABLED,
        )

        spigotSessionCache = component.sessionCache()
        permissionsManager = component.permissionsManager()

        val logger = component.logger()
        val pluginMessageListener = SpigotMessageListener(logger)
        server.messenger.registerOutgoingPluginChannel(this, Channel.BUNGEECORD)
        server.messenger.registerIncomingPluginChannel(this, Channel.BUNGEECORD, pluginMessageListener)

        commandRegistry = SpigotCommandRegistry(plugin = this, logger)
        listenerRegistry = SpigotListenerRegistry(plugin = this, logger).also {
            it.register(component.pendingJoinActionListener())
        }

        component.modules().forEach { module ->
            module.spigotCommands.forEach { commandRegistry?.register(it) }
            module.spigotListeners.forEach { listenerRegistry?.register(it) }
            module.spigotSubChannelListeners.forEach { pluginMessageListener.register(it) }
        }
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(this)
        server.messenger.unregisterIncomingPluginChannel(this)

        listenerRegistry?.unregisterAll()

        spigotSessionCache = null
        commandRegistry = null
        listenerRegistry = null
        permissionsManager = null
    }
}
