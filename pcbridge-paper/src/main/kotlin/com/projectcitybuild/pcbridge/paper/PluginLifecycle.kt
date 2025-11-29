package com.projectcitybuild.pcbridge.paper

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.chat.listeners.AsyncChatListener
import com.projectcitybuild.pcbridge.paper.architecture.connection.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners.CoroutineExceptionListener
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListingDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.listeners.ServerListPingListener
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.listeners.PlayerStateListener
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabPlaceholders
import com.projectcitybuild.pcbridge.paper.architecture.tablist.listeners.TabListeners
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.MaxPlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.OnlinePlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerAFKPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerNamePlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerPingPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerWorldPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.catching
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.architecture.commands.registerCommands
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.commands.BanCommand
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.listeners.BanDialogListener
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.listeners.BanWebhookListener
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.middleware.BanConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.building.hooks.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.paper.features.building.hooks.commands.ItemNameCommand
import com.projectcitybuild.pcbridge.paper.features.building.hooks.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.paper.features.building.hooks.listeners.InvisFrameListener
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.BuildCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.BuildsCommand
import com.projectcitybuild.pcbridge.paper.features.chatbadge.hooks.decorators.ChatBadgeDecorator
import com.projectcitybuild.pcbridge.paper.features.chatbadge.hooks.listeners.ChatBadgeInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.decorators.ChatEmojiDecorator
import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.listeners.EmojiConfigListener
import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.decorators.ChatUrlDecorator
import com.projectcitybuild.pcbridge.paper.features.config.hooks.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.features.config.hooks.listeners.ConfigWebhookListener
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.decorators.ChatGroupDecorator
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.listener.ChatGroupInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.listener.RoleStateChangeListener
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.placeholders.TabGroupListPlaceholder
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.placeholders.TabGroupsPlaceholder
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.HomeCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.HomesCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.listeners.HomeRenameDialogListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners.ServerOverviewJoinListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.commands.MaintenanceCommand
import com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.decorators.MaintenanceMotdDecorator
import com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.listener.MaintenanceReminderListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.middleware.MaintenanceConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.randomteleport.hooks.commands.RtpCommand
import com.projectcitybuild.pcbridge.paper.features.register.commands.CodeCommand
import com.projectcitybuild.pcbridge.paper.features.register.commands.RegisterCommand
import com.projectcitybuild.pcbridge.paper.features.register.listeners.VerifyCodeDialogListener
import com.projectcitybuild.pcbridge.paper.features.serverlinks.listeners.ServerLinkListener
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.HubCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.SetSpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.SpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.listeners.PlayerRespawnListener
import com.projectcitybuild.pcbridge.paper.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.commands.SyncDebugCommand
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.listener.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.paper.features.warnings.commands.WarnCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.listeners.WarpRenameDialogListener
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.listeners.WarpWebhookListener
import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.ItemTextListener
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.AnvilCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.CartographyTableCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.EnchantingCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.WorkbenchCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.GrindstoneCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.LoomCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.SmithingTableCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.StoneCutterCommand
import com.projectcitybuild.pcbridge.paper.integrations.dynmap.DynmapIntegration
import com.projectcitybuild.pcbridge.paper.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.pcbridge.paper.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.pcbridge.webserver.HttpServer
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class PluginLifecycle : KoinComponent {
    private val plugin: JavaPlugin = get()
    private val errorTracker: ErrorTracker by inject()
    private val listenerRegistry: SpigotListenerRegistry by inject()
    private val httpServer: HttpServer by inject()
    private val remoteConfig: RemoteConfig by inject()
    private val store: Store by inject()
    private val otel: OpenTelemetryProvider by inject()

    suspend fun boot() = errorTracker.catching {
        TracerFactory.configure(otel)

        httpServer.start()
        remoteConfig.fetch()
        store.hydrate()

        registerMiddleware()
        registerDecorators()
        registerTabPlaceholders()
        registerCommands()
        registerListeners()

        get<DynmapIntegration>().enable()
        get<EssentialsIntegration>().enable()
        get<LuckPermsIntegration>().enable()
    }

    suspend fun shutdown() = errorTracker.catching {
        httpServer.stop()
        store.persist()

        get<SpigotTimer>().cancelAll()

        get<DynmapIntegration>().disable()
        get<EssentialsIntegration>().disable()
        get<LuckPermsIntegration>().disable()

        listenerRegistry.unregisterAll()
    }

    private fun registerMiddleware() = get<ConnectionMiddlewareChain>().register(
        get<BanConnectionMiddleware>(),
        get<MaintenanceConnectionMiddleware>(),
    )

    private fun registerCommands() = plugin.registerCommands(
        get<AnvilCommand>(),
        get<BanCommand>(),
        get<BuildCommand>(),
        get<BuildsCommand>(),
        get<CartographyTableCommand>(),
        get<CodeCommand>(),
        get<ConfigCommand>(),
        get<EnchantingCommand>(),
        get<GrindstoneCommand>(),
        get<HomeCommand>(),
        get<HomesCommand>(),
        get<HubCommand>(),
        get<InvisFrameCommand>(),
        get<ItemNameCommand>(),
        get<LoomCommand>(),
        get<MaintenanceCommand>(),
        get<NightVisionCommand>(),
        get<RegisterCommand>(),
        get<RtpCommand>(),
        get<SetSpawnCommand>(),
        get<SmithingTableCommand>(),
        get<SpawnCommand>(),
        get<StaffChatCommand>(),
        get<StoneCutterCommand>(),
        get<SyncCommand>(),
        get<SyncDebugCommand>(),
        get<WarpCommand>(),
        get<WarpsCommand>(),
        get<WarnCommand>(),
        get<WorkbenchCommand>(),
    )

    private fun registerListeners() = listenerRegistry.register(
        get<AnnounceJoinListener>(),
        get<AnnounceQuitListener>(),
        get<AnnouncementConfigListener>(),
        get<AnnouncementEnableListener>(),
        get<AsyncChatListener>(),
        get<AuthorizeConnectionListener>(),
        get<BanWebhookListener>(),
        get<BanDialogListener>(),
        get<ChatBadgeInvalidateListener>(),
        get<ChatGroupInvalidateListener>(),
        get<ConfigWebhookListener>(),
        get<CoroutineExceptionListener>(),
        get<EmojiConfigListener>(),
        get<FirstTimeJoinListener>(),
        get<HomeRenameDialogListener>(),
        get<InvisFrameListener>(),
        get<ItemTextListener>(),
        get<MaintenanceReminderListener>(),
        get<PlayerRespawnListener>(),
        get<PlayerStateListener>(),
        get<PlayerSyncRequestListener>(),
        get<ServerOverviewJoinListener>(),
        get<ServerListPingListener>(),
        get<ServerLinkListener>(),
        get<RoleStateChangeListener>(),
        get<TabListeners>(),
        get<TelemetryPlayerConnectListener>(),
        get<VerifyCodeDialogListener>(),
        get<WarpRenameDialogListener>(),
        get<WarpWebhookListener>(),
    )

    private fun registerDecorators() {
        get<ChatDecoratorChain>().apply{
            senders(
                get<ChatGroupDecorator>(),
                get<ChatBadgeDecorator>(),
            )
            messages(
                get<ChatEmojiDecorator>(),
                get<ChatUrlDecorator>(),
            )
        }
        get<ServerListingDecoratorChain>().register(
            get<MaintenanceMotdDecorator>(),
        )
    }

    private fun registerTabPlaceholders() {
        get<TabPlaceholders>().apply {
            sections(
                get<OnlinePlayerCountPlaceholder>(),
                get<MaxPlayerCountPlaceholder>(),
                get<PlayerWorldPlaceholder>(),
                get<TabGroupListPlaceholder>(),
            )
            players(
                get<PlayerNamePlaceholder>(),
                get<PlayerAFKPlaceholder>(),
                get<PlayerPingPlaceholder>(),
                get<TabGroupsPlaceholder>(),
            )
        }
    }
}