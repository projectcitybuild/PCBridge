package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext.startKoin

class PCBridge : SuspendingJavaPlugin() {
    private var container: KoinApplication? = null

    override suspend fun onEnableAsync() {
        printLogo()

        val module = pluginModule(this)
        val container = startKoin {
            modules(module)
        }
        this.container = container

        Lifecycle().boot()
    }

    override suspend fun onDisableAsync() {
        Lifecycle().shutdown()

        this.container?.close()
        this.container = null

        logger.info("Goodbye")
    }

    private fun printLogo() = logo
        .split("\n")
        .forEach(logger::info)
}

private class Lifecycle: KoinComponent {
    private val sentry: SentryReporter = get()
    private val logger: PlatformLogger = get()

    fun boot() = sentry.runCatching {
        get<JavaPlugin>().apply {
            getCommand("warps")!!.setSuspendingExecutor(get<WarpsCommand>())
        }
    }.onFailure {
        sentry.report(it)
        logger.severe(it.localizedMessage)
    }

    fun shutdown() = sentry.runCatching {
        // TODO
    }.onFailure {
        sentry.report(it)
        logger.severe(it.localizedMessage)
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