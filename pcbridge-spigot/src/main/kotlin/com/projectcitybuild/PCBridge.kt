package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.features.utilities.commands.PCBridgeCommand
import com.projectcitybuild.features.warps.commands.WarpCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.integrations.DynmapIntegration
import com.projectcitybuild.integrations.EssentialsIntegration
import com.projectcitybuild.support.spigot.SpigotCommandRegistry
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.event.HandlerList
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

    suspend fun boot() = trace {
        commandRegistry.register(
            handler = get<PCBridgeCommand>(),
            argsParser = PCBridgeCommand.Args.Parser(),
            tabCompleter = get<PCBridgeCommand.TabCompleter>(),
        )
        commandRegistry.register(
            handler = get<WarpCommand>(),
            argsParser = WarpCommand.Args.Parser(),
        )
        commandRegistry.register(
            handler = get<WarpsCommand>(),
            argsParser = WarpsCommand.Args.Parser(),
        )

        get<DynmapIntegration>().onEnable()
        get<EssentialsIntegration>().onEnable()
    }

    suspend fun shutdown() = trace {
        get<DynmapIntegration>().onDisable()
        get<EssentialsIntegration>().onDisable()

        // Unregister all event listeners
        HandlerList.unregisterAll()

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