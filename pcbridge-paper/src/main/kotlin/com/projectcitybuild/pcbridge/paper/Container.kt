package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.http.DiscordHttp
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.JsonStorage
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.DateTimeFormatter
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.adapters.LuckPermsPermissions
import com.projectcitybuild.pcbridge.paper.features.config.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.paper.features.bans.actions.AuthorizeConnection
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.BanWebhookListener
import com.projectcitybuild.pcbridge.paper.features.bans.repositories.PlayerRepository
import com.projectcitybuild.pcbridge.paper.features.chat.ChatBadgeFormatter
import com.projectcitybuild.pcbridge.paper.features.chat.ChatGroupFormatter
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.ChatConfigListener
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.FormatNameChatListener
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.SyncPlayerChatListener
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatGroupRepository
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
import com.projectcitybuild.pcbridge.paper.features.groups.actions.SyncPlayerGroups
import com.projectcitybuild.pcbridge.paper.features.groups.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.groups.listener.SyncRankListener
import com.projectcitybuild.pcbridge.paper.architecture.state.listeners.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.repositories.TelemetryRepository
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.PCBHttp
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
import com.projectcitybuild.pcbridge.paper.integrations.DynmapIntegration
import com.projectcitybuild.pcbridge.paper.integrations.EssentialsIntegration
import com.projectcitybuild.pcbridge.paper.integrations.LuckPermsIntegration
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners.CoroutineExceptionListener
import com.projectcitybuild.pcbridge.paper.architecture.webhooks.WebServerDelegate
import com.projectcitybuild.pcbridge.paper.core.libs.discord.DiscordSend
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildEditCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildSetCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildUnvoteCommand
import com.projectcitybuild.pcbridge.paper.features.config.listeners.ConfigWebhookListener
import com.projectcitybuild.pcbridge.paper.features.groups.listener.PlayerSyncWebhookListener
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
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

fun pluginModule(_plugin: JavaPlugin) =
    module {
        spigot(_plugin)
        core()
        http()
        webServer()
        integrations()

        // Features
        announcements()
        architecture()
        bans()
        building()
        builds()
        chat()
        config()
        groups()
        joinMessages()
        invisFrames()
        register()
        staffChat()
        telemetry()
        warps()
        watchdog()
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

    single<Permissions> {
        LuckPermsPermissions()
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
            jsonStorage = JsonStorage(
                file = get<JavaPlugin>()
                    .dataFolder
                    .resolve("config.json"),
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

    single { Store() }

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
            sentry = get(),
            warpRepository = get(),
        )
    }

    single {
        EssentialsIntegration(
            plugin = get(),
            sentry = get(),
        )
    }

    single {
        LuckPermsIntegration()
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

private fun Module.architecture() {
    factory {
        PlayerStateListener(
            store = get(),
            time = get(),
            eventBroadcaster = get(),
        )
    }

    factory {
        PlayerSyncRequestListener(
            store = get(),
            time = get(),
            server = get(),
            eventBroadcaster = get(),
            playerRepository = get(),
        )
    }

    factory {
        CoroutineExceptionListener(
            sentryReporter = get(),
        )
    }
}

private fun Module.bans() {
    factory {
        PlayerRepository(
            httpService = get<PCBHttp>().player,
        )
    }

    factory {
        AuthorizeConnectionListener(
            playerRepository = get(),
            authorizeConnection = AuthorizeConnection(),
            sentry = get(),
            eventBroadcaster = get(),
        )
    }

    factory {
        BanWebhookListener(
            server = get(),
        )
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

private fun Module.chat() {
    factory {
        ChatBadgeRepository(
            store = get(),
            remoteConfig = get(),
            badgeFormatter = get(),
            badgeCache = get(named("badge_cache")),
        )
    }

    factory {
        ChatGroupRepository(
            chatGroupFormatter = get(),
            store = get(),
            groupCache = get(named("group_cache")),
        )
    }

    single(named("badge_cache")) {
        Cache.Builder<UUID, ChatBadgeRepository.CachedComponent>().build()
    }

    single(named("group_cache")) {
        Cache.Builder<UUID, ChatGroupRepository.CachedComponent>().build()
    }

    factory {
        ChatBadgeFormatter()
    }

    single {
        ChatGroupFormatter()
    }

    factory {
        FormatNameChatListener(
            chatBadgeRepository = get(),
            chatGroupRepository = get(),
        )
    }

    factory {
        SyncPlayerChatListener(
            chatGroupRepository = get(),
            chatBadgeRepository = get(),
        )
    }

    factory {
        ChatConfigListener(
            chatGroupRepository = get(),
            chatBadgeRepository = get(),
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

private fun Module.staffChat() {
    factory {
        StaffChatCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            remoteConfig = get(),
        )
    }
}

private fun Module.groups() {
    factory {
        SyncPlayerGroups(
            permissions = get(),
        )
    }

    factory {
        SyncRankListener(
            syncPlayerGroups = get(),
        )
    }

    factory {
        PlayerSyncWebhookListener(
            eventBroadcaster = get(),
        )
    }

    factory {
        SyncCommand(
            plugin = get<JavaPlugin>(),
            eventBroadcaster = get(),
        )
    }
}
