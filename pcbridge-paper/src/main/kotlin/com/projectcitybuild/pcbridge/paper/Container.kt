package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.http.discord.DiscordHttp
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.JsonStorage
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.DateTimeFormatter
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.features.config.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.paper.features.bans.actions.CheckBan
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.BanWebhookListener
import com.projectcitybuild.pcbridge.paper.features.sync.repositories.PlayerRepository
import com.projectcitybuild.pcbridge.paper.features.groups.ChatGroupFormatter
import com.projectcitybuild.pcbridge.paper.features.groups.listener.ChatGroupInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.groups.repositories.ChatGroupRepository
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
import com.projectcitybuild.pcbridge.paper.features.sync.actions.SyncPlayer
import com.projectcitybuild.pcbridge.paper.features.sync.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.groups.listener.RoleStateChangeListener
import com.projectcitybuild.pcbridge.paper.features.sync.listener.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.repositories.TelemetryRepository
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.architecture.PlayerDataProvider
import com.projectcitybuild.pcbridge.paper.architecture.chat.listeners.AsyncChatListener
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.connection.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildsCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildCreateCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildListCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildMoveCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildVoteCommand
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.ItemTextListener
import com.projectcitybuild.pcbridge.paper.features.building.commands.ItemNameCommand
import com.projectcitybuild.pcbridge.paper.integrations.dynmap.DynmapIntegration
import com.projectcitybuild.pcbridge.paper.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.pcbridge.paper.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners.CoroutineExceptionListener
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PersistedServerState
import com.projectcitybuild.pcbridge.paper.architecture.webhooks.WebServerDelegate
import com.projectcitybuild.pcbridge.paper.core.libs.discord.DiscordSend
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.architecture.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListingDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.listeners.ServerListPingListener
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabPlaceholders
import com.projectcitybuild.pcbridge.paper.architecture.tablist.listeners.TabListeners
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.MaxPlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.OnlinePlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerAFKPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerNamePlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerPingPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerWorldPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.cooldowns.Cooldown
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.features.randomteleport.actions.FindRandomLocation
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.SafeYLocationFinder
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.storage.TeleportHistoryStorage
import com.projectcitybuild.pcbridge.paper.features.groups.RolesFilter
import com.projectcitybuild.pcbridge.paper.core.utils.PeriodicRunner
import com.projectcitybuild.pcbridge.paper.features.chatbadge.ChatBadgeFormatter
import com.projectcitybuild.pcbridge.paper.features.chatbadge.listeners.ChatBadgeInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.chatbadge.decorators.ChatBadgeDecorator
import com.projectcitybuild.pcbridge.paper.features.chatbadge.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.bans.commands.BanCommand
import com.projectcitybuild.pcbridge.paper.features.bans.middleware.BanConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildEditCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildSetCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildUnvoteCommand
import com.projectcitybuild.pcbridge.paper.features.chatemojis.decorators.ChatEmojiDecorator
import com.projectcitybuild.pcbridge.paper.features.groups.decorators.ChatGroupDecorator
import com.projectcitybuild.pcbridge.paper.features.chaturls.decorators.ChatUrlDecorator
import com.projectcitybuild.pcbridge.paper.features.config.listeners.ConfigWebhookListener
import com.projectcitybuild.pcbridge.paper.features.groups.placeholders.TabGroupListPlaceholder
import com.projectcitybuild.pcbridge.paper.features.groups.placeholders.TabGroupsPlaceholder
import com.projectcitybuild.pcbridge.paper.features.homes.commands.HomeCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.HomesCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeCreateCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeRenameCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeLimitCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeListCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeMoveCommand
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.features.sync.commands.SyncDebugCommand
import com.projectcitybuild.pcbridge.paper.features.maintenance.commands.MaintenanceCommand
import com.projectcitybuild.pcbridge.paper.features.maintenance.listener.MaintenanceReminderListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.decorators.MaintenanceMotdDecorator
import com.projectcitybuild.pcbridge.paper.features.maintenance.middleware.MaintenanceConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.randomteleport.commands.RtpCommand
import com.projectcitybuild.pcbridge.paper.features.serverlinks.listeners.ServerLinkListener
import com.projectcitybuild.pcbridge.paper.features.spawns.commands.SetSpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.commands.SpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.data.SerializableSpawn
import com.projectcitybuild.pcbridge.paper.features.spawns.listeners.PlayerRespawnListener
import com.projectcitybuild.pcbridge.paper.features.spawns.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.features.warnings.commands.WarnCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpReloadCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.paper.features.warps.listeners.WarpWebhookListener
import com.projectcitybuild.pcbridge.webserver.HttpServer
import com.projectcitybuild.pcbridge.webserver.data.HttpServerConfig
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

fun pluginModule(_plugin: JavaPlugin) =
    module {
        spigot(_plugin)
        core()
        http()
        webServer()
        integrations()
        architecture()

        // Features
        announcements()
        bans()
        building()
        builds()
        chatBadge()
        chatEmojis()
        chatUrls()
        config()
        groups()
        homes()
        joinMessages()
        invisFrames()
        maintenance()
        randomTeleport()
        register()
        serverLinks()
        spawn()
        staffChat()
        sync()
        telemetry()
        warps()
        watchdog()
        warnings()
    }

private fun Module.spigot(plugin: JavaPlugin) {
    single { plugin }

    factory {
        get<JavaPlugin>().server
    }

    single {
        BukkitAudiences.create(get<JavaPlugin>())
    }

    single {
        SpigotNamespace(
            plugin = get(),
        )
    }

    single {
        SpigotListenerRegistry(
            plugin = get(),
        )
    }

    factory {
        SpigotTimer(
            plugin = get(),
        )
    }

    factory {
        SpigotEventBroadcaster(
            server = get(),
            minecraftDispatcher =  { get<JavaPlugin>().minecraftDispatcher },
        )
    }
}

private fun Module.core() {
    single {
        LocalConfig(
            file = get<JavaPlugin>()
                .dataFolder
                .resolve("config.json"),
            jsonStorage = JsonStorage(
                typeToken = object : TypeToken<LocalConfigKeyValues>() {},
            ),
        )
    }

    single {
        SentryReporter(
            localConfig = get(),
        ).apply {
            val localConfigProvider = get<LocalConfig>()
            val config = localConfigProvider.get()
            if (config.errorReporting.isSentryEnabled) {
                start()
            }
        }
    } onClose {
        it?.close()
    }

    factory {
        val config = get<RemoteConfig>().latest.config
        val zoneId = ZoneId.of(config.localization.timeZone)

        LocalizedTime(
            clock = Clock.system(zoneId),
        )
    }

    factory {
        val config = get<RemoteConfig>().latest.config

        DateTimeFormatter(
            locale =
                Locale.forLanguageTag(
                    config.localization.locale,
                ),
            timezone =
                ZoneId.of(
                    config.localization.timeZone,
                ),
        )
    }

    single {
        Store(
            file = get<JavaPlugin>()
                .dataFolder
                .resolve("cache/server_state.json"),
            jsonStorage = JsonStorage(
                typeToken = object : TypeToken<PersistedServerState>() {},
            ),
        )
    }

    single {
        RemoteConfig(
            configHttpService = get<PCBHttp>().config,
            eventBroadcaster = get(),
        )
    }

    single {
        DiscordSend(
            localConfig = get(),
            discordHttpService = get<DiscordHttp>().discord,
            sentryReporter = get(),
            periodicRunner = PeriodicRunner(processInterval = 10.seconds)
        )
    }

    factory {
        ManageUrlGenerator(
            server = get(),
            localConfig = get(),
        )
    }

    single {
        Permissions()
    }

    factory {
        PlayerTeleporter(
            safeYLocationFinder = get(),
            teleportHistoryStorage = get(),
        )
    }

    factory {
        TeleportHistoryStorage(
            eventBroadcaster = get(),
        )
    }

    factory {
        SafeYLocationFinder()
    }

    single {
        Cooldown(
            timer = get(),
        )
    }
}

private fun Module.webServer() {
    single {
        val localConfig = get<LocalConfig>().get()

        HttpServer(
            config = HttpServerConfig(
                authToken = localConfig.webServer.token,
                port = localConfig.webServer.port,
            ),
            webhookDelegate = WebServerDelegate(
                eventBroadcaster = get(),
            ),
        )
    }
}

private fun Module.http() {
    single {
        val localConfig = get<LocalConfig>().get()

        PCBHttp(
            authToken = localConfig.api.token,
            baseURL = localConfig.api.baseUrl,
            withLogging = localConfig.api.isLoggingEnabled,
        )
    }

    single {
        val localConfig = get<LocalConfig>().get()

        DiscordHttp(
            withLogging = localConfig.api.isLoggingEnabled,
        )
    }
}

private fun Module.integrations() {
    single {
        DynmapIntegration(
            plugin = get(),
            remoteConfig = get(),
            warpRepository = get(),
        )
    }

    single {
        EssentialsIntegration(
            plugin = get(),
            server = get(),
            sentry = get(),
            store = get(),
            eventBroadcaster = get(),
            tabRenderer = get(),
        )
    }

    single {
        LuckPermsIntegration(
            permissions = get(),
        )
    }
}

private fun Module.announcements() {
    single {
        AnnouncementRepository(
            remoteConfig = get(),
            store = get(),
        )
    }

    single {
        StartAnnouncementTimer(
            repository = get(),
            remoteConfig = get(),
            timer = get(),
            server = get(),
        )
    }

    factory {
        AnnouncementEnableListener(
            announcementTimer = get(),
            plugin = get(),
        )
    }

    factory {
        AnnouncementConfigListener(
            announcementTimer = get(),
        )
    }
}

private fun Module.architecture() {
    factory {
        PlayerStateListener(
            store = get(),
            time = get(),
            eventBroadcaster = get(),
        )
    }

    factory {
        CoroutineExceptionListener(
            sentryReporter = get(),
        )
    }

    single {
        ConnectionMiddlewareChain()
    }

    factory {
        AuthorizeConnectionListener(
            middlewareChain = get(),
            playerDataProvider = get(),
            sentry = get(),
            eventBroadcaster = get(),
        )
    }

    single {
        ChatDecoratorChain()
    }

    factory {
        AsyncChatListener(
            decorators = get(),
        )
    }

    single {
        ServerListingDecoratorChain()
    }

    factory {
        ServerListPingListener(
            remoteConfig = get(),
            decorators = get(),
        )
    }

    single {
        Permissions()
    }

    single {
        TabRenderer(
            remoteConfig = get(),
            tabPlaceholders = get(),
        )
    }

    single {
        TabPlaceholders(
            listenerRegistry = get(),
        )
    }

    factory {
        TabListeners(
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        MaxPlayerCountPlaceholder(
            server = get(),
        )
    }

    factory {
        OnlinePlayerCountPlaceholder(
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        PlayerAFKPlaceholder(
            server = get(),
            tabRenderer = get(),
            store = get(),
        )
    }

    factory {
        PlayerNamePlaceholder()
    }

    factory {
        PlayerPingPlaceholder(
            plugin = get<JavaPlugin>(),
            server = get(),
            tabRenderer = get(),
            spigotTimer = get(),
        )
    }

    factory {
        PlayerWorldPlaceholder(
            tabRenderer = get(),
        )
    }
}

private fun Module.bans() {
    factory {
        BanConnectionMiddleware(
            checkBan = CheckBan(),
        )
    }

    factory {
        BanWebhookListener(
            server = get(),
        )
    }

    factory {
        BanCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            manageUrlGenerator = get(),
        )
    }
}

private fun Module.builds() {
    factory {
        BuildsCommand(
            buildListCommand = BuildListCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildCreateCommand = BuildCreateCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildMoveCommand = BuildMoveCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildVoteCommand = BuildVoteCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildUnvoteCommand = BuildUnvoteCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildDeleteCommand = BuildDeleteCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildEditCommand = BuildEditCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildSetCommand = BuildSetCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
        )
    }

    factory {
        BuildCommand(
            plugin = get<JavaPlugin>(),
            buildRepository = get(),
            server = get(),
            playerTeleporter = get(),
        )
    }

    single {
        BuildRepository(
            buildHttpService = get<PCBHttp>().builds
        )
    }
}

private fun Module.building() {
    factory {
        NightVisionCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        ItemNameCommand(
            plugin = get<JavaPlugin>(),
            eventBroadcaster = get(),
        )
    }
}

private fun Module.chatBadge() {
    factory {
        ChatBadgeInvalidateListener(
            chatBadgeRepository = get(),
        )
    }

    factory {
        ChatBadgeRepository(
            store = get(),
            remoteConfig = get(),
            badgeFormatter = get(),
            badgeCache = get(named("badge_cache")),
        )
    }

    factory {
        ChatBadgeFormatter()
    }

    single(named("badge_cache")) {
        Cache.Builder<UUID, ChatBadgeRepository.CachedComponent>().build()
    }

    factory {
        ChatBadgeDecorator(
            chatBadgeRepository = get(),
        )
    }
}

private fun Module.chatEmojis() {
    factory {
        ChatEmojiDecorator()
    }
}

private fun Module.chatUrls() {
    factory {
        ChatUrlDecorator()
    }
}

private fun Module.config() {
    factory {
        ConfigCommand(
            plugin = get<JavaPlugin>(),
            remoteConfig = get(),
        )
    }

    factory {
        ConfigWebhookListener(
            remoteConfig = get(),
        )
    }
}

private fun Module.groups() {
    factory {
        ChatGroupInvalidateListener(
            chatGroupRepository = get(),
        )
    }

    factory {
        RoleStateChangeListener(
            permissions = get(),
        )
    }

    factory {
        ChatGroupDecorator(
            chatGroupRepository = get(),
        )
    }

    factory {
        TabGroupListPlaceholder(
            rolesFilter = get(),
            store = get(),
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        TabGroupsPlaceholder(
            chatGroupRepository = get(),
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        ChatGroupRepository(
            chatGroupFormatter = get(),
            store = get(),
            groupCache = get(named("group_cache")),
        )
    }

    single(named("group_cache")) {
        Cache.Builder<UUID, ChatGroupRepository.CachedComponent>().build()
    }

    factory {
        ChatGroupFormatter(
            rolesFilter = get(),
        )
    }

    factory {
        RolesFilter()
    }
}

private fun Module.invisFrames() {
    factory {
        InvisFrameCommand(
            plugin = get<JavaPlugin>(),
            spigotNamespace = get(),
        )
    }

    factory {
        FramePlaceListener(
            spigotNamespace = get(),
        )
    }

    factory {
        FrameItemInsertListener(
            spigotNamespace = get(),
        )
    }

    factory {
        FrameItemRemoveListener(
            spigotNamespace = get(),
        )
    }
}

private fun Module.joinMessages() {
    factory {
        AnnounceJoinListener(
            remoteConfig = get(),
        )
    }

    factory {
        AnnounceQuitListener(
            remoteConfig = get(),
            store = get(),
            time = get(),
        )
    }

    factory {
        FirstTimeJoinListener(
            remoteConfig = get(),
            server = get(),
            store = get(),
        )
    }

    factory {
        ServerOverviewJoinListener(
            remoteConfig = get(),
        )
    }
}

private fun Module.maintenance() {
    factory {
        MaintenanceConnectionMiddleware(
            store = get(),
        )
    }

    factory {
        MaintenanceMotdDecorator(
            store = get(),
        )
    }

    factory {
        MaintenanceReminderListener(
            store = get(),
            server = get(),
            timer = get(),
        )
    }

    factory {
        MaintenanceCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            store = get(),
            eventBroadcaster = get(),
        )
    }
}

private fun Module.randomTeleport() {
    factory {
        RtpCommand(
            plugin = get<JavaPlugin>(),
            findRandomLocation = get(),
            cooldown = get(),
        )
    }

    factory {
        FindRandomLocation(
            playerTeleporter = get(),
        )
    }
}

private fun Module.serverLinks() {
    factory {
        ServerLinkListener(
            remoteConfig = get(),
        )
    }
}

private fun Module.spawn() {
    factory {
        SpawnCommand(
            plugin = get<JavaPlugin>(),
            spawnRepository = get(),
            playerTeleporter = get(),
        )
    }

    factory {
        SetSpawnCommand(
            plugin = get<JavaPlugin>(),
            spawnRepository = get(),
        )
    }

    factory {
        PlayerRespawnListener(
            spawnRepository = get(),
        )
    }

    single {
        SpawnRepository(
            storage = JsonStorage(
                typeToken = object : TypeToken<SerializableSpawn>() {},
            ),
        )
    }
}

private fun Module.staffChat() {
    factory {
        StaffChatCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            remoteConfig = get(),
            decorators = get(),
        )
    }
}

private fun Module.register() {
    factory {
        RegisterCommand(
            plugin = get<JavaPlugin>(),
            registerHttpService = get<PCBHttp>().register,
        )
    }
    factory {
        CodeCommand(
            plugin = get<JavaPlugin>(),
            registerHttpService = get<PCBHttp>().register,
        )
    }
}

private fun Module.telemetry() {
    factory {
        TelemetryRepository(
            telemetryHttpService = get<PCBHttp>().telemetry,
        )
    }

    factory {
        TelemetryPlayerConnectListener(
            telemetryRepository = get(),
        )
    }
}

private fun Module.sync() {
    factory {
        SyncPlayer(
            store = get(),
            time = get(),
            server = get(),
            eventBroadcaster = get(),
            playerRepository = get(),
        )
    }

    factory {
        SyncCommand(
            plugin = get<JavaPlugin>(),
            syncPlayer = get(),
        )
    }

    factory {
        SyncDebugCommand(
            plugin = get<JavaPlugin>(),
            permissions = get(),
        )
    }

    factory {
        PlayerSyncRequestListener(
            syncPlayer = get(),
        )
    }

    factory {
        PlayerRepository(
            httpService = get<PCBHttp>().player,
        )
    }.bind<PlayerDataProvider>()
}

private fun Module.homes() {
    single {
        HomeRepository(
            homeHttpService = get<PCBHttp>().homes,
        )
    }

    factory {
        HomeCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            playerTeleporter = get(),
            homeRepository = get(),
        )
    }

    factory {
        HomesCommand(
            homeListCommand = HomeListCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeCreateCommand = HomeCreateCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeMoveCommand = HomeMoveCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeDeleteCommand = HomeDeleteCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeLimitCommand = HomeLimitCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeRenameCommand = HomeRenameCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
        )
    }
}

private fun Module.warnings() {
    factory {
        WarnCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            manageUrlGenerator = get(),
        )
    }
}

private fun Module.warps() {
    single {
        WarpRepository(
            warpHttpService = get<PCBHttp>().warps
        )
    }

    factory {
        WarpCommand(
            plugin = get<JavaPlugin>(),
            warpRepository = get(),
            server = get(),
            playerTeleporter = get(),
        )
    }

    factory {
        WarpsCommand(
            createCommand = WarpCreateCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
                server = get(),
            ),
            deleteCommand = WarpDeleteCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
                server = get(),
            ),
            listCommand = WarpListCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
                remoteConfig = get(),
            ),
            moveCommand = WarpMoveCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
            ),
            reloadCommand = WarpReloadCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
            ),
            renameCommand = WarpRenameCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
            ),
        )
    }

    factory {
        WarpWebhookListener(
            warpRepository = get(),
        )
    }
}

private fun Module.watchdog() {
    factory {
        ItemTextListener(
            discordSend = get(),
            time = get(),
        )
    }
}
