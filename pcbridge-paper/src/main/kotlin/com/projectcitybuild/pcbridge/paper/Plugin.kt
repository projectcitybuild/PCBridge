package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.projectcitybuild.pcbridge.paper.core.libs.discord.DiscordSend
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.libs.errors.trace
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.IPBanRequestListener
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.UUIDBanRequestListener
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildsCommand
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.ChatConfigListener
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.FormatNameChatListener
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.SyncPlayerChatListener
import com.projectcitybuild.pcbridge.paper.features.groups.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.paper.features.invisframes.listeners.FrameItemInsertListener
import com.projectcitybuild.pcbridge.paper.features.invisframes.listeners.FrameItemRemoveListener
import com.projectcitybuild.pcbridge.paper.features.invisframes.listeners.FramePlaceListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.pcbridge.paper.features.building.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.paper.architecture.listeners.PlayerStateListener
import com.projectcitybuild.pcbridge.paper.features.register.commands.CodeCommand
import com.projectcitybuild.pcbridge.paper.features.register.commands.RegisterCommand
import com.projectcitybuild.pcbridge.paper.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.paper.features.groups.listener.SyncRankListener
import com.projectcitybuild.pcbridge.paper.architecture.listeners.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.ItemTextListener
import com.projectcitybuild.pcbridge.paper.features.building.commands.ItemNameCommand
import com.projectcitybuild.pcbridge.paper.integrations.DynmapIntegration
import com.projectcitybuild.pcbridge.paper.integrations.EssentialsIntegration
import com.projectcitybuild.pcbridge.paper.integrations.LuckPermsIntegration
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.architecture.listeners.ExceptionListener
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.register
import com.projectcitybuild.pcbridge.webserver.HttpServer
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin

class Plugin : SuspendingJavaPlugin() {
    private var container: KoinApplication? = null

    override suspend fun onEnableAsync() {
        printLogo()

        val module = pluginModule(this)
        this.container = startKoin { modules(module) }

        Lifecycle().boot().onFailure {
            server.pluginManager.disablePlugin(this)
        }
    }

    override suspend fun onDisableAsync() {
        Lifecycle().shutdown()

        this.container?.close()
        this.container = null
        stopKoin()

        logger.info("Goodbye")
    }

    private fun printLogo() =
        logo
            .split("\n")
            .forEach(logger::info)
}

private class Lifecycle : KoinComponent {
    private val audiences: BukkitAudiences = get()
    private val sentry: SentryReporter by inject()
    private val listenerRegistry: SpigotListenerRegistry by inject()
    private val httpServer: HttpServer by inject()
    private val remoteConfig: RemoteConfig by inject()

    suspend fun boot() =
        sentry.trace {
            httpServer.start()

            remoteConfig.fetch()

            // TODO: inject lifecycle manager instead
            get<JavaPlugin>()
                .lifecycleManager
                .registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                    event.registrar().register(
                        get<BuildsCommand>(),
                        get<BuildCommand>(),
                        get<WarpCommand>(),
                        get<WarpsCommand>(),
                        get<StaffChatCommand>(),
                        get<NightVisionCommand>(),
                        get<InvisFrameCommand>(),
                        get<RegisterCommand>(),
                        get<CodeCommand>(),
                        get<SyncCommand>(),
                        get<ConfigCommand>(),
                        get<ItemNameCommand>(),
                    )
                }

            listenerRegistry.register(
                get<AnnounceJoinListener>(),
                get<AnnounceQuitListener>(),
                get<AnnouncementConfigListener>(),
                get<AnnouncementEnableListener>(),
                get<AuthorizeConnectionListener>(),
                get<ChatConfigListener>(),
                get<ExceptionListener>(),
                get<FirstTimeJoinListener>(),
                get<FormatNameChatListener>(),
                get<FramePlaceListener>(),
                get<FrameItemInsertListener>(),
                get<FrameItemRemoveListener>(),
                get<IPBanRequestListener>(),
                get<PlayerStateListener>(),
                get<PlayerSyncRequestListener>(),
                get<ServerOverviewJoinListener>(),
                get<ItemTextListener>(),
                get<SyncPlayerChatListener>(),
                get<SyncRankListener>(),
                get<TelemetryPlayerConnectListener>(),
                get<UUIDBanRequestListener>(),
            )

            get<DynmapIntegration>().enable()
            get<EssentialsIntegration>().enable()
            get<LuckPermsIntegration>().enable()

            get<DiscordSend>().startProcessing()
        }

    suspend fun shutdown() =
        sentry.trace {
            httpServer.stop()

            get<SpigotTimer>().cancelAll()

            get<DynmapIntegration>().disable()
            get<EssentialsIntegration>().disable()
            get<LuckPermsIntegration>().disable()

            get<DiscordSend>().stopProcessing()

            listenerRegistry.unregisterAll()
            audiences.close()
        }
}

private val logo =
    """
    
    ██████╗  ██████╗██████╗ ██████╗ ██╗██████╗  ██████╗ ███████╗
    ██╔══██╗██╔════╝██╔══██╗██╔══██╗██║██╔══██╗██╔════╝ ██╔════╝
    ██████╔╝██║     ██████╔╝██████╔╝██║██║  ██║██║  ███╗█████╗  
    ██╔═══╝ ██║     ██╔══██╗██╔══██╗██║██║  ██║██║   ██║██╔══╝  
    ██║     ╚██████╗██████╔╝██║  ██║██║██████╔╝╚██████╔╝███████╗
    ╚═╝      ╚═════╝╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝  ╚═════╝ ╚══════╝
    
    """.trimIndent()
