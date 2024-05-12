package com.projectcitybuild.pcbridge

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.core.config.JsonStorage
import com.projectcitybuild.pcbridge.core.database.DatabaseSession
import com.projectcitybuild.pcbridge.core.database.DatabaseSource
import com.projectcitybuild.pcbridge.core.datetime.DateTimeFormatter
import com.projectcitybuild.pcbridge.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.core.permissions.Permissions
import com.projectcitybuild.pcbridge.core.permissions.adapters.LuckPermsPermissions
import com.projectcitybuild.pcbridge.core.state.Store
import com.projectcitybuild.pcbridge.data.PluginConfig
import com.projectcitybuild.pcbridge.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.features.bans.actions.AuthoriseConnection
import com.projectcitybuild.pcbridge.features.bans.actions.BanIP
import com.projectcitybuild.pcbridge.features.bans.actions.BanUUID
import com.projectcitybuild.pcbridge.features.bans.actions.CheckUUIDBan
import com.projectcitybuild.pcbridge.features.bans.actions.UnbanIP
import com.projectcitybuild.pcbridge.features.bans.actions.UnbanUUID
import com.projectcitybuild.pcbridge.features.bans.commands.BanCommand
import com.projectcitybuild.pcbridge.features.bans.commands.BanIPCommand
import com.projectcitybuild.pcbridge.features.bans.commands.CheckBanCommand
import com.projectcitybuild.pcbridge.features.bans.commands.UnbanCommand
import com.projectcitybuild.pcbridge.features.bans.commands.UnbanIPCommand
import com.projectcitybuild.pcbridge.features.bans.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.features.bans.repositories.AggregateRepository
import com.projectcitybuild.pcbridge.features.bans.repositories.IPBanRepository
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerUUIDRepository
import com.projectcitybuild.pcbridge.features.chat.ChatBadgeFormatter
import com.projectcitybuild.pcbridge.features.chat.ChatGroupFormatter
import com.projectcitybuild.pcbridge.features.chat.listeners.EmojiChatListener
import com.projectcitybuild.pcbridge.features.chat.listeners.FormatNameChatListener
import com.projectcitybuild.pcbridge.features.chat.listeners.SyncBadgesOnJoinListener
import com.projectcitybuild.pcbridge.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.features.chat.repositories.ChatGroupRepository
import com.projectcitybuild.pcbridge.features.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.features.invisframes.listeners.FrameItemInsertListener
import com.projectcitybuild.pcbridge.features.invisframes.listeners.FrameItemRemoveListener
import com.projectcitybuild.pcbridge.features.invisframes.listeners.FramePlaceListener
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.pcbridge.features.joinmessages.repositories.PlayerConfigRepository
import com.projectcitybuild.pcbridge.features.mute.commands.MuteCommand
import com.projectcitybuild.pcbridge.features.mute.commands.UnmuteCommand
import com.projectcitybuild.pcbridge.features.mute.listeners.MuteChatListener
import com.projectcitybuild.pcbridge.features.nightvision.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.features.sync.actions.GenerateAccountVerificationURL
import com.projectcitybuild.pcbridge.features.sync.actions.SyncPlayerGroups
import com.projectcitybuild.pcbridge.features.sync.actions.UpdatePlayerGroups
import com.projectcitybuild.pcbridge.features.sync.commands.SyncCommand
import com.projectcitybuild.pcbridge.features.sync.commands.SyncOtherCommand
import com.projectcitybuild.pcbridge.features.sync.listener.SyncRankOnJoinListener
import com.projectcitybuild.pcbridge.features.sync.repositories.SyncRepository
import com.projectcitybuild.pcbridge.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.features.telemetry.repositories.TelemetryRepository
import com.projectcitybuild.pcbridge.features.utilities.commands.PCBridgeCommand
import com.projectcitybuild.pcbridge.features.warnings.actions.GetUnacknowledgedWarnings
import com.projectcitybuild.pcbridge.features.warnings.commands.WarningAcknowledgeCommand
import com.projectcitybuild.pcbridge.features.warnings.listeners.NotifyWarningsOnJoinListener
import com.projectcitybuild.pcbridge.features.warnings.repositories.PlayerWarningRepository
import com.projectcitybuild.pcbridge.features.warps.Warp
import com.projectcitybuild.pcbridge.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.HttpService
import com.projectcitybuild.pcbridge.integrations.DynmapIntegration
import com.projectcitybuild.pcbridge.integrations.EssentialsIntegration
import com.projectcitybuild.pcbridge.integrations.LuckPermsIntegration
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommandRegistry
import com.projectcitybuild.pcbridge.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.support.spigot.SpigotTimer
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

fun pluginModule(_plugin: JavaPlugin) =
    module {
        spigot(_plugin)
        core()
        http()
        integrations()

        // Features
        announcements()
        bans()
        chat()
        joinMessages()
        invisFrames()
        mute()
        nightVision()
        staffChat()
        sync()
        telemetry()
        utilities()
        warps()
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
}

private fun Module.core() {
    single {
        Config(
            jsonStorage =
                JsonStorage(
                    file =
                        get<JavaPlugin>()
                            .dataFolder
                            .resolve("config.json"),
                    typeToken = object : TypeToken<PluginConfig>() {},
                ),
        )
    }

    single {
        DatabaseSession().apply {
            val configProvider = get<Config>()
            val config = configProvider.get()
            connect(DatabaseSource.fromConfig(config))
        }
    } withOptions {
        createdAtStart()
        onClose { it?.disconnect() }
    }

    single {
        SentryReporter(
            config = get(),
        ).apply {
            val configProvider = get<Config>()
            val config = configProvider.get()
            if (config.errorReporting.isSentryEnabled) {
                start()
            }
        }
    } onClose {
        it?.close()
    }

    factory {
        val config = get<Config>().get()
        val zoneId = ZoneId.of(config.localization.timeZone)

        LocalizedTime(
            clock = Clock.system(zoneId),
        )
    }

    factory {
        val config = get<Config>().get()

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
}

private fun Module.http() {
    single {
        val config = get<Config>().get()

        HttpService(
            authToken = config.api.token,
            baseURL = config.api.baseUrl,
            withLogging = config.api.isLoggingEnabled,
        )
    }
}

private fun Module.integrations() {
    single {
        DynmapIntegration(
            plugin = get(),
            config = get(),
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
            config = get(),
            store = get(),
        )
    }

    factory {
        AnnouncementEnableListener(
            announcementRepository = get(),
            config = get(),
            timer = get(),
            server = get(),
            plugin = get(),
        )
    }
}

private fun Module.warps() {
    single {
        WarpRepository(
            db = get(),
            cache =
                Cache.Builder<String, Warp>()
                    .expireAfterWrite(30.minutes)
                    .build(),
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
            config = get(),
            server = get(),
            time = get(),
        )
    }
}

private fun Module.joinMessages() {
    factory {
        PlayerConfigRepository(
            dataSource = get(),
        )
    }

    factory {
        AnnounceJoinListener(
            config = get(),
            store = get(),
            time = get(),
        )
    }

    factory {
        AnnounceQuitListener(
            config = get(),
            store = get(),
            time = get(),
        )
    }

    factory {
        FirstTimeJoinListener(
            config = get(),
            server = get(),
            playerConfigRepository = get(),
            time = get(),
        )
    }

    factory {
        ServerOverviewJoinListener(
            config = get(),
        )
    }
}

private fun Module.bans() {
    factory {
        AggregateRepository(
            httpService = get<HttpService>().aggregate,
        )
    }

    factory {
        PlayerBanRepository(
            httpService = get<HttpService>().uuidBan,
        )
    }

    factory {
        PlayerUUIDRepository(
            server = get(),
            httpService = get<HttpService>().playerUuid,
        )
    }

    factory {
        IPBanRepository(
            httpService = get<HttpService>().ipBan,
        )
    }

    factory {
        BanCommand(
            banUUID =
                BanUUID(
                    playerBanRepository = get(),
                    playerUUIDRepository = get(),
                    server = get(),
                ),
            server = get(),
        )
    }

    factory {
        BanIPCommand(
            banIP =
                BanIP(
                    ipBanRepository = get(),
                    server = get(),
                ),
            server = get(),
        )
    }

    factory {
        UnbanCommand(
            unbanUUID =
                UnbanUUID(
                    playerBanRepository = get(),
                    playerUUIDRepository = get(),
                ),
        )
    }

    factory {
        UnbanIPCommand(
            unbanIP =
                UnbanIP(
                    ipBanRepository = get(),
                ),
        )
    }

    factory {
        CheckBanCommand(
            checkUUIDBan =
                CheckUUIDBan(
                    dateTimeFormatter = get(),
                    playerUUIDRepository = get(),
                    playerBanRepository = get(),
                ),
        )
    }

    factory {
        AuthorizeConnectionListener(
            aggregateRepository = get(),
            authoriseConnection = AuthoriseConnection(),
            dateTimeFormatter = get(),
            sentry = get(),
            server = get(),
            minecraftDispatcher = { get<JavaPlugin>().minecraftDispatcher },
            store = get(),
        )
    }
}

private fun Module.mute() {
    single(named("mute_cache")) {
        // We use a regular cache instead of the server state here
        // so that mutes persist even if the player leaves, but
        // invalidates if the plugin is reloaded/shutdown
        Cache.Builder<UUID, Unit>().build()
    }

    factory {
        MuteCommand(
            server = get(),
            mutedPlayers = get(named("mute_cache")),
        )
    }

    factory {
        UnmuteCommand(
            server = get(),
            mutedPlayers = get(named("mute_cache")),
        )
    }

    factory {
        MuteChatListener(
            mutedPlayers = get(named("mute_cache")),
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
            config = get(),
        )
    }

    factory {
        ChatGroupRepository(
            permissions = get(),
            config = get(),
        )
    }

    single(named("badge_cache")) {
        Cache.Builder<UUID, Component>().build()
    }

    single(named("group_cache")) {
        Cache.Builder<UUID, ChatGroupFormatter.Aggregate>().build()
    }

    factory {
        ChatBadgeFormatter(
            chatBadgeRepository = get(),
        )
    }

    factory {
        ChatGroupFormatter(
            chatGroupRepository = get(),
        )
    }

    factory {
        EmojiChatListener()
    }

    factory {
        FormatNameChatListener(
            chatGroupFormatter = get(),
            chatBadgeFormatter = get(),
            badgeCache = get(named("badge_cache")),
            groupCache = get(named("group_cache")),
        )
    }

    factory {
        SyncBadgesOnJoinListener(
            store = get(),
        )
    }
}

private fun Module.telemetry() {
    factory {
        TelemetryRepository(
            telemetryHttpService = get<HttpService>().telemetry,
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
        )
    }
}

private fun Module.sync() {
    factory {
        SyncRepository(
            playerGroupHttpService = get<HttpService>().playerGroup,
            accountLinkHttpService = get<HttpService>().verificationURL,
            config = get(),
        )
    }

    factory {
        UpdatePlayerGroups(
            permissions = get(),
            syncRepository = get(),
        )
    }

    factory {
        GenerateAccountVerificationURL(
            syncRepository = get(),
        )
    }

    factory {
        SyncPlayerGroups(
            permissions = get(),
            syncRepository = get(),
        )
    }

    factory {
        SyncCommand(
            generateAccountVerificationURL = get(),
            updatePlayerGroups = get(),
        )
    }

    factory {
        SyncOtherCommand(
            server = get(),
            updatePlayerGroups = get(),
        )
    }

    factory {
        SyncRankOnJoinListener(
            syncPlayerGroups = get(),
        )
    }
}

private fun Module.utilities() {
    factory {
        PCBridgeCommand(
            plugin = get(),
        )
    }

    factory {
        PCBridgeCommand.TabCompleter()
    }
}

private fun Module.warnings() {
    factory {
        PlayerWarningRepository(
            playerWarningHttpService = get<HttpService>().playerWarning,
        )
    }

    factory {
        WarningAcknowledgeCommand(
            warningRepository = get(),
        )
    }

    factory {
        NotifyWarningsOnJoinListener(
            getUnacknowledgedWarnings =
                GetUnacknowledgedWarnings(
                    dateTimeFormatter = get(),
                    warningRepository = get(),
                ),
        )
    }
}
