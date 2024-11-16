package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.http.DiscordHttp
import com.projectcitybuild.pcbridge.paper.core.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.localconfig.JsonStorage
import com.projectcitybuild.pcbridge.paper.core.datetime.DateTimeFormatter
import com.projectcitybuild.pcbridge.paper.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.core.permissions.adapters.LuckPermsPermissions
import com.projectcitybuild.pcbridge.paper.core.remoteconfig.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.store.Store
import com.projectcitybuild.pcbridge.paper.core.localconfig.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.paper.features.bans.actions.AuthorizeConnection
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.IPBanRequestListener
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.UUIDBanRequestListener
import com.projectcitybuild.pcbridge.paper.features.bans.repositories.PlayerRepository
import com.projectcitybuild.pcbridge.paper.features.chat.ChatBadgeFormatter
import com.projectcitybuild.pcbridge.paper.features.chat.ChatGroupFormatter
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.ChatConfigListener
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.FormatNameChatListener
import com.projectcitybuild.pcbridge.paper.features.chat.listeners.SyncPlayerChatListener
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatGroupRepository
import com.projectcitybuild.pcbridge.paper.features.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.paper.features.invisframes.listeners.FrameItemInsertListener
import com.projectcitybuild.pcbridge.paper.features.invisframes.listeners.FrameItemRemoveListener
import com.projectcitybuild.pcbridge.paper.features.invisframes.listeners.FramePlaceListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.pcbridge.paper.features.nightvision.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.paper.features.playerstate.listeners.PlayerStateListener
import com.projectcitybuild.pcbridge.paper.features.register.commands.CodeCommand
import com.projectcitybuild.pcbridge.paper.features.register.commands.RegisterCommand
import com.projectcitybuild.pcbridge.paper.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.paper.features.groups.actions.SyncPlayerGroups
import com.projectcitybuild.pcbridge.paper.features.groups.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.groups.listener.SyncRankListener
import com.projectcitybuild.pcbridge.paper.features.playerstate.listeners.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.repositories.TelemetryRepository
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.PCBHttp
import com.projectcitybuild.pcbridge.paper.core.discord.services.DiscordSend
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildsCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildListCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.ItemTextListener
import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.commands.ItemNameCommand
import com.projectcitybuild.pcbridge.paper.integrations.DynmapIntegration
import com.projectcitybuild.pcbridge.paper.integrations.EssentialsIntegration
import com.projectcitybuild.pcbridge.paper.integrations.LuckPermsIntegration
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotCommandRegistry
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.webserver.HttpServer
import com.projectcitybuild.pcbridge.webserver.HttpServerConfig
import io.github.reactivecircus.cache4k.Cache
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
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
        bans()
        builds()
        chat()
        groups()
        joinMessages()
        invisFrames()
        nightVision()
        playerState()
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
        SpigotCommandRegistry(
            plugin = get(),
            sentry = get(),
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

    factory {
        ConfigCommand(
            remoteConfig = get(),
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
            delegate = WebServerDelegate(
                eventBroadcaster = get(),
                remoteConfig = get(),
                warpRepository = get(),
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
            buildListCommand = get(),
        )
    }

    factory {
        BuildListCommand(
            buildRepository = get(),
        )
    }

    factory {
        BuildCommand(
            buildRepository = get(),
        )
    }

    single {
        BuildRepository(
            buildHttpService = get<PCBHttp>().builds
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
            warpRepository = get(),
            server = get(),
        )
    }

    factory {
        WarpsCommand(
            warpRepository = get(),
            remoteConfig = get(),
            server = get(),
            time = get(),
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

    factory {
        ItemNameCommand(
            eventBroadcaster = get(),
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

private fun Module.playerState() {
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
        UUIDBanRequestListener(
            server = get(),
        )
    }

    factory {
        IPBanRequestListener(
            server = get(),
        )
    }
}

private fun Module.nightVision() {
    factory {
        NightVisionCommand()
    }
}

private fun Module.invisFrames() {
    factory {
        InvisFrameCommand(
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
            registerHttpService = get<PCBHttp>().register,
        )
    }
    factory {
        CodeCommand(
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
        SyncCommand(
            server = get(),
            eventBroadcaster = get(),
        )
    }
}
