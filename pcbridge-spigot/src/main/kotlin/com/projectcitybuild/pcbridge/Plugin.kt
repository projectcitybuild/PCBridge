package com.projectcitybuild.pcbridge

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.projectcitybuild.pcbridge.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.core.errors.trace
import com.projectcitybuild.pcbridge.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.features.bans.commands.BanCommand
import com.projectcitybuild.pcbridge.features.bans.commands.BanIPCommand
import com.projectcitybuild.pcbridge.features.bans.commands.CheckBanCommand
import com.projectcitybuild.pcbridge.features.bans.commands.UnbanCommand
import com.projectcitybuild.pcbridge.features.bans.commands.UnbanIPCommand
import com.projectcitybuild.pcbridge.features.bans.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.features.chat.listeners.EmojiChatListener
import com.projectcitybuild.pcbridge.features.chat.listeners.FormatNameChatListener
import com.projectcitybuild.pcbridge.features.chat.listeners.SyncBadgesOnJoinListener
import com.projectcitybuild.pcbridge.features.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.features.invisframes.listeners.FrameItemInsertListener
import com.projectcitybuild.pcbridge.features.invisframes.listeners.FrameItemRemoveListener
import com.projectcitybuild.pcbridge.features.invisframes.listeners.FramePlaceListener
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.pcbridge.features.mute.commands.MuteCommand
import com.projectcitybuild.pcbridge.features.mute.commands.UnmuteCommand
import com.projectcitybuild.pcbridge.features.mute.listeners.MuteChatListener
import com.projectcitybuild.pcbridge.features.nightvision.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.features.sync.commands.SyncCommand
import com.projectcitybuild.pcbridge.features.sync.commands.SyncOtherCommand
import com.projectcitybuild.pcbridge.features.sync.listener.SyncRankOnJoinListener
import com.projectcitybuild.pcbridge.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.features.utilities.commands.PCBridgeCommand
import com.projectcitybuild.pcbridge.features.warnings.commands.WarningAcknowledgeCommand
import com.projectcitybuild.pcbridge.features.warnings.listeners.NotifyWarningsOnJoinListener
import com.projectcitybuild.pcbridge.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.integrations.DynmapIntegration
import com.projectcitybuild.pcbridge.integrations.EssentialsIntegration
import com.projectcitybuild.pcbridge.integrations.LuckPermsIntegration
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommandRegistry
import com.projectcitybuild.pcbridge.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.support.spigot.SpigotTimer
import net.kyori.adventure.platform.bukkit.BukkitAudiences
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
        val container = startKoin {
            modules(module)
        }
        this.container = container

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

    private fun printLogo() = logo
        .split("\n")
        .forEach(logger::info)
}

private class Lifecycle: KoinComponent {
    private val audiences: BukkitAudiences = get()
    private val sentry: SentryReporter by inject()
    private val commandRegistry: SpigotCommandRegistry by inject()
    private val listenerRegistry: SpigotListenerRegistry by inject()

    suspend fun boot() = sentry.trace {
        commandRegistry.apply {
            register(
                handler = get<PCBridgeCommand>(),
                argsParser = PCBridgeCommand.Args.Parser(),
                tabCompleter = get<PCBridgeCommand.TabCompleter>(),
            )
            register(
                handler = get<WarpCommand>(),
                argsParser = WarpCommand.Args.Parser(),
            )
            register(
                handler = get<WarpsCommand>(),
                argsParser = WarpsCommand.Args.Parser(),
            )
            register(
                handler = get<StaffChatCommand>(),
                argsParser = StaffChatCommand.Args.Parser(),
            )
            register(
                handler = get<MuteCommand>(),
                argsParser = MuteCommand.Args.Parser(),
            )
            register(
                handler = get<UnmuteCommand>(),
                argsParser = UnmuteCommand.Args.Parser(),
            )
            register(
                handler = get<NightVisionCommand>(),
                argsParser = NightVisionCommand.Args.Parser(),
            )
            register(
                handler = get<InvisFrameCommand>(),
                argsParser = InvisFrameCommand.Args.Parser(),
            )
            register(
                handler = get<BanCommand>(),
                argsParser = BanCommand.Args.Parser(),
            )
            register(
                handler = get<BanIPCommand>(),
                argsParser = BanIPCommand.Args.Parser(),
            )
            register(
                handler = get<CheckBanCommand>(),
                argsParser = CheckBanCommand.Args.Parser(),
            )
            register(
                handler = get<UnbanCommand>(),
                argsParser = UnbanCommand.Args.Parser(),
            )
            register(
                handler = get<UnbanIPCommand>(),
                argsParser = UnbanIPCommand.Args.Parser(),
            )
            register(
                handler = get<SyncCommand>(),
                argsParser = SyncCommand.Args.Parser(),
            )
            register(
                handler = get<SyncOtherCommand>(),
                argsParser = SyncOtherCommand.Args.Parser(),
            )
            register(
                handler = get<WarningAcknowledgeCommand>(),
                argsParser = WarningAcknowledgeCommand.Args.Parser(),
            )
        }
        listenerRegistry.register(
            get<AnnounceJoinListener>(),
            get<AnnounceQuitListener>(),
            get<FirstTimeJoinListener>(),
            get<ServerOverviewJoinListener>(),
            get<EmojiChatListener>(),
            get<TelemetryPlayerConnectListener>(),
            get<MuteChatListener>(),
            get<FramePlaceListener>(),
            get<FrameItemInsertListener>(),
            get<FrameItemRemoveListener>(),
            get<AuthorizeConnectionListener>(),
            get<SyncRankOnJoinListener>(),
            get<SyncBadgesOnJoinListener>(),
            get<FormatNameChatListener>(),
            get<AnnouncementEnableListener>(),
            get<NotifyWarningsOnJoinListener>(),
        )

        get<DynmapIntegration>().enable()
        get<EssentialsIntegration>().enable()
        get<LuckPermsIntegration>().enable()
    }

    suspend fun shutdown() = sentry.trace {
        get<SpigotTimer>().cancelAll()

        get<DynmapIntegration>().disable()
        get<EssentialsIntegration>().disable()
        get<LuckPermsIntegration>().disable()

        listenerRegistry.unregisterAll()
        commandRegistry.unregisterAll()
        audiences.close()
    }
}

private val logo = """
        
        ██████╗  ██████╗██████╗ ██████╗ ██╗██████╗  ██████╗ ███████╗
        ██╔══██╗██╔════╝██╔══██╗██╔══██╗██║██╔══██╗██╔════╝ ██╔════╝
        ██████╔╝██║     ██████╔╝██████╔╝██║██║  ██║██║  ███╗█████╗  
        ██╔═══╝ ██║     ██╔══██╗██╔══██╗██║██║  ██║██║   ██║██╔══╝  
        ██║     ╚██████╗██████╔╝██║  ██║██║██████╔╝╚██████╔╝███████╗
        ╚═╝      ╚═════╝╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝  ╚═════╝ ╚══════╝
        
    """.trimIndent()