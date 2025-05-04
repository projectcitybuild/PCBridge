package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.projectcitybuild.pcbridge.paper.architecture.chat.listeners.AsyncChatListener
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddlewareChain
import com.projectcitybuild.pcbridge.paper.architecture.connection.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.core.libs.discord.DiscordSend
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.libs.errors.trace
import com.projectcitybuild.pcbridge.paper.features.config.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.BanWebhookListener
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildsCommand
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.ChatConfigListener
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.SyncPlayerChatListener
import com.projectcitybuild.pcbridge.paper.features.groups.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.building.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.paper.features.building.listeners.FrameItemInsertListener
import com.projectcitybuild.pcbridge.paper.features.building.listeners.FrameItemRemoveListener
import com.projectcitybuild.pcbridge.paper.features.building.listeners.FramePlaceListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.pcbridge.paper.features.building.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.paper.architecture.state.listeners.PlayerStateListener
import com.projectcitybuild.pcbridge.paper.features.register.commands.CodeCommand
import com.projectcitybuild.pcbridge.paper.features.register.commands.RegisterCommand
import com.projectcitybuild.pcbridge.paper.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.paper.features.groups.listener.SyncRankListener
import com.projectcitybuild.pcbridge.paper.architecture.state.listeners.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.ItemTextListener
import com.projectcitybuild.pcbridge.paper.features.building.commands.ItemNameCommand
import com.projectcitybuild.pcbridge.paper.integrations.dynmap.DynmapIntegration
import com.projectcitybuild.pcbridge.paper.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.pcbridge.paper.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners.CoroutineExceptionListener
import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.register
import com.projectcitybuild.pcbridge.paper.features.badge.listeners.BadgeInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.badge.middleware.ChatBadgeMiddleware
import com.projectcitybuild.pcbridge.paper.features.bans.commands.BanCommand
import com.projectcitybuild.pcbridge.paper.features.bans.middleware.BanConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.chat.middleware.ChatEmojiMiddleware
import com.projectcitybuild.pcbridge.paper.features.chat.middleware.ChatGroupMiddleware
import com.projectcitybuild.pcbridge.paper.features.chat.middleware.ChatUrlMiddleware
import com.projectcitybuild.pcbridge.paper.features.config.listeners.ConfigWebhookListener
import com.projectcitybuild.pcbridge.paper.features.groups.commands.SyncDebugCommand
import com.projectcitybuild.pcbridge.paper.features.groups.listener.PlayerSyncWebhookListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.commands.MaintenanceCommand
import com.projectcitybuild.pcbridge.paper.features.maintenance.listener.MaintenanceReminderListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.listener.MaintenanceMotdListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.middleware.MaintenanceConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.motd.listeners.MotdListener
import com.projectcitybuild.pcbridge.paper.features.tab.listeners.TabNameListener
import com.projectcitybuild.pcbridge.paper.features.teleport.commands.RtpCommand
import com.projectcitybuild.pcbridge.paper.features.warnings.commands.WarnCommand
import com.projectcitybuild.pcbridge.paper.features.warps.listeners.WarpWebhookListener
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
    private val store: Store by inject()

    suspend fun boot() =
        sentry.trace {
            httpServer.start()
            remoteConfig.fetch()
            store.hydrate()

            get<ConnectionMiddlewareChain>().register(
                get<BanConnectionMiddleware>(),
                get<MaintenanceConnectionMiddleware>(),
            )
            get<ChatMiddlewareChain>().register(
                get<ChatEmojiMiddleware>(),
                get<ChatUrlMiddleware>(),
                get<ChatGroupMiddleware>(),
                get<ChatBadgeMiddleware>(),
            )

            get<JavaPlugin>()
                .lifecycleManager
                .registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                    event.registrar().register(
                        get<BanCommand>(),
                        get<BuildCommand>(),
                        get<BuildsCommand>(),
                        get<CodeCommand>(),
                        get<ConfigCommand>(),
                        get<InvisFrameCommand>(),
                        get<ItemNameCommand>(),
                        get<MaintenanceCommand>(),
                        get<NightVisionCommand>(),
                        get<RegisterCommand>(),
                        get<RtpCommand>(),
                        get<StaffChatCommand>(),
                        get<SyncCommand>(),
                        get<SyncDebugCommand>(),
                        get<WarpCommand>(),
                        get<WarpsCommand>(),
                        get<WarnCommand>(),
                    )
                }

            listenerRegistry.register(
                get<AnnounceJoinListener>(),
                get<AnnounceQuitListener>(),
                get<AnnouncementConfigListener>(),
                get<AnnouncementEnableListener>(),
                get<AsyncChatListener>(),
                get<AuthorizeConnectionListener>(),
                get<BanWebhookListener>(),
                get<BadgeInvalidateListener>(),
                get<ChatConfigListener>(),
                get<ConfigWebhookListener>(),
                get<CoroutineExceptionListener>(),
                get<FirstTimeJoinListener>(),
                get<FramePlaceListener>(),
                get<FrameItemInsertListener>(),
                get<FrameItemRemoveListener>(),
                get<ItemTextListener>(),
                get<MaintenanceReminderListener>(),
                get<MaintenanceMotdListener>(),
                get<MotdListener>(),
                get<PlayerStateListener>(),
                get<PlayerSyncRequestListener>(),
                get<PlayerSyncWebhookListener>(),
                get<ServerOverviewJoinListener>(),
                get<SyncPlayerChatListener>(),
                get<SyncRankListener>(),
                get<TabNameListener>(),
                get<TelemetryPlayerConnectListener>(),
                get<WarpWebhookListener>(),
            )

            get<DynmapIntegration>().enable()
            get<EssentialsIntegration>().enable()
            get<LuckPermsIntegration>().enable()
        }

    suspend fun shutdown() =
        sentry.trace {
            httpServer.stop()
            store.persist()

            get<SpigotTimer>().cancelAll()

            get<DynmapIntegration>().disable()
            get<EssentialsIntegration>().disable()
            get<LuckPermsIntegration>().disable()

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
