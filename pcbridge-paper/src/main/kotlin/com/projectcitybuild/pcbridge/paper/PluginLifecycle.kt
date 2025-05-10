package com.projectcitybuild.pcbridge.paper

import com.projectcitybuild.pcbridge.paper.architecture.chat.listeners.AsyncChatListener
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.connection.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners.CoroutineExceptionListener
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListingDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.listeners.ServerListPingListener
import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.listeners.PlayerStateListener
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabPlaceholders
import com.projectcitybuild.pcbridge.paper.architecture.tablist.listeners.TabListeners
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.MaxPlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.OnlinePlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerAFKPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerNamePlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerPingPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerWorldPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.libs.errors.trace
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.register
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.bans.commands.BanCommand
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.BanWebhookListener
import com.projectcitybuild.pcbridge.paper.features.bans.middleware.BanConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.building.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.paper.features.building.commands.ItemNameCommand
import com.projectcitybuild.pcbridge.paper.features.building.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.paper.features.building.listeners.FrameItemInsertListener
import com.projectcitybuild.pcbridge.paper.features.building.listeners.FrameItemRemoveListener
import com.projectcitybuild.pcbridge.paper.features.building.listeners.FramePlaceListener
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildsCommand
import com.projectcitybuild.pcbridge.paper.features.chatbadge.listeners.ChatBadgeInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.chatbadge.decorators.ChatBadgeDecorator
import com.projectcitybuild.pcbridge.paper.features.chatemojis.decorators.ChatEmojiDecorator
import com.projectcitybuild.pcbridge.paper.features.chaturls.decorators.ChatUrlDecorator
import com.projectcitybuild.pcbridge.paper.features.config.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.features.config.listeners.ConfigWebhookListener
import com.projectcitybuild.pcbridge.paper.features.groups.listener.ChatGroupInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.groups.listener.RoleStateChangeListener
import com.projectcitybuild.pcbridge.paper.features.groups.decorators.ChatGroupDecorator
import com.projectcitybuild.pcbridge.paper.features.groups.placeholders.TabGroupPlaceholder
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.commands.MaintenanceCommand
import com.projectcitybuild.pcbridge.paper.features.maintenance.decorators.MaintenanceMotdDecorator
import com.projectcitybuild.pcbridge.paper.features.maintenance.listener.MaintenanceReminderListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.middleware.MaintenanceConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.register.commands.CodeCommand
import com.projectcitybuild.pcbridge.paper.features.register.commands.RegisterCommand
import com.projectcitybuild.pcbridge.paper.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.paper.features.sync.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.sync.commands.SyncDebugCommand
import com.projectcitybuild.pcbridge.paper.features.sync.listener.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.paper.features.randomteleport.commands.RtpCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.commands.SetSpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.commands.SpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.listeners.PlayerRespawnListener
import com.projectcitybuild.pcbridge.paper.features.warnings.commands.WarnCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.warps.listeners.WarpWebhookListener
import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.ItemTextListener
import com.projectcitybuild.pcbridge.paper.integrations.dynmap.DynmapIntegration
import com.projectcitybuild.pcbridge.paper.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.pcbridge.paper.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.pcbridge.webserver.HttpServer
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class PluginLifecycle : KoinComponent {
    private val audiences: BukkitAudiences = get()
    private val sentry: SentryReporter by inject()
    private val listenerRegistry: SpigotListenerRegistry by inject()
    private val httpServer: HttpServer by inject()
    private val remoteConfig: RemoteConfig by inject()
    private val store: Store by inject()

    suspend fun boot() = sentry.trace {
        httpServer.start()
        remoteConfig.fetch()
        store.hydrate()

        get<ConnectionMiddlewareChain>().register(
            get<BanConnectionMiddleware>(),
            get<MaintenanceConnectionMiddleware>(),
        )
        get<ChatDecoratorChain>().apply{
            addSender(
                get<ChatGroupDecorator>(),
                get<ChatBadgeDecorator>(),
            )
            addMessage(
                get<ChatEmojiDecorator>(),
                get<ChatUrlDecorator>(),
            )
        }
        get<ServerListingDecoratorChain>().register(
            get<MaintenanceMotdDecorator>(),
        )
        get<TabPlaceholders>().apply {
            section(get<OnlinePlayerCountPlaceholder>())
            section(get<MaxPlayerCountPlaceholder>())
            section(get<PlayerWorldPlaceholder>())
            section(get<TabGroupPlaceholder>())

            player(get<PlayerNamePlaceholder>())
            player(get<PlayerAFKPlaceholder>())
            player(get<PlayerPingPlaceholder>())
        }

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
                    get<SetSpawnCommand>(),
                    get<SpawnCommand>(),
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
            get<ChatBadgeInvalidateListener>(),
            get<ChatGroupInvalidateListener>(),
            get<ConfigWebhookListener>(),
            get<CoroutineExceptionListener>(),
            get<FirstTimeJoinListener>(),
            get<FramePlaceListener>(),
            get<FrameItemInsertListener>(),
            get<FrameItemRemoveListener>(),
            get<ItemTextListener>(),
            get<MaintenanceReminderListener>(),
            get<PlayerRespawnListener>(),
            get<PlayerStateListener>(),
            get<PlayerSyncRequestListener>(),
            get<ServerOverviewJoinListener>(),
            get<ServerListPingListener>(),
            get<RoleStateChangeListener>(),
            get<TabListeners>(),
            get<TelemetryPlayerConnectListener>(),
            get<WarpWebhookListener>(),
        )

        get<DynmapIntegration>().enable()
        get<EssentialsIntegration>().enable()
        get<LuckPermsIntegration>().enable()
    }

    suspend fun shutdown() = sentry.trace {
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