package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.github.shynixn.mccoroutine.bukkit.setSuspendingTabCompleter
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.features.utilities.commands.PCBridgeCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.java.JavaPlugin
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

    private val plugin: JavaPlugin by inject()

    fun boot() = trace {
        plugin.apply {
            // TODO: wrap these later for Sentry reporting
            getCommand("pcbridge")!!.setSuspendingExecutor(get<PCBridgeCommand>())
            getCommand("pcbridge")!!.setSuspendingTabCompleter(get<PCBridgeCommand.TabCompleter>())

            getCommand("warps")!!.setSuspendingExecutor(get<WarpsCommand>())
        }
    }

    fun shutdown() = trace {
        plugin.apply {
            getCommand("pcbridge")!!.setExecutor(null)
            getCommand("pcbridge")!!.tabCompleter = null

            getCommand("warps")!!.setExecutor(null)
        }
        audiences.close()
    }

    private fun <R> trace(block: () -> R): Result<R> {
        return runCatching(block).onFailure {
            sentry.report(it)
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