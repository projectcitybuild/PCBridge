package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.features.chat.listeners.EmojiChatListener
import com.projectcitybuild.features.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.features.invisframes.listeners.FrameItemInsertListener
import com.projectcitybuild.features.invisframes.listeners.FrameItemRemoveListener
import com.projectcitybuild.features.invisframes.listeners.FramePlaceListener
import com.projectcitybuild.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.features.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.features.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.features.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.features.mute.commands.MuteCommand
import com.projectcitybuild.features.mute.commands.UnmuteCommand
import com.projectcitybuild.features.mute.listeners.MuteChatListener
import com.projectcitybuild.features.nightvision.commands.NightVisionCommand
import com.projectcitybuild.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.features.utilities.commands.PCBridgeCommand
import com.projectcitybuild.features.warps.commands.WarpCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.integrations.DynmapIntegration
import com.projectcitybuild.integrations.EssentialsIntegration
import com.projectcitybuild.support.spigot.SpigotCommandRegistry
import com.projectcitybuild.support.spigot.SpigotListenerRegistry
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin

class PCBridge : SuspendingJavaPlugin() {
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

    suspend fun boot() = trace {
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
        }
        listenerRegistry.apply {
            register(get<AnnounceJoinListener>())
            register(get<AnnounceQuitListener>())
            register(get<FirstTimeJoinListener>())
            register(get<ServerOverviewJoinListener>())
            register(get<EmojiChatListener>())
            register(get<TelemetryPlayerConnectListener>())
            register(get<MuteChatListener>())
            register(get<FramePlaceListener>())
            register(get<FrameItemInsertListener>())
            register(get<FrameItemRemoveListener>())
        }

        get<DynmapIntegration>().onEnable()
        get<EssentialsIntegration>().onEnable()
    }

    suspend fun shutdown() = trace {
        get<DynmapIntegration>().onDisable()
        get<EssentialsIntegration>().onDisable()

        listenerRegistry.unregisterAll()
        commandRegistry.unregisterAll()
        audiences.close()
    }

    private suspend fun <R> trace(block: suspend () -> R): Result<R> {
        return runCatching { block() }.onFailure {
            sentry.report(it)
            throw it
        }
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