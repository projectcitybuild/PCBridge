package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin

class Plugin : SuspendingJavaPlugin() {
    private var container: KoinApplication? = null

    override suspend fun onEnableAsync() {
        printLogo()

        val moduleList = pluginModules(this)
        this.container = startKoin { modules(moduleList) }

        PluginLifecycle().boot().onFailure {
            server.pluginManager.disablePlugin(this)
        }
    }

    override suspend fun onDisableAsync() {
        PluginLifecycle().shutdown()

        this.container?.close()
        this.container = null
        stopKoin()

        logger.info("Goodbye")
    }

    private fun printLogo() =
        logo
            .split("\n")
            .forEach(logger::info)
}

private val logo =
    """
    
    ██████╗  ██████╗██████╗ ██████╗ ██╗██████╗  ██████╗ ███████╗
    ██╔══██╗██╔════╝██╔══██╗██╔══██╗██║██╔══██╗██╔════╝ ██╔════╝
    ██████╔╝██║     ██████╔╝██████╔╝██║██║  ██║██║  ███╗█████╗  
    ██╔═══╝ ██║     ██╔══██╗██╔══██╗██║██║  ██║██║   ██║██╔══╝  
    ██║     ╚██████╗██████╔╝██║  ██║██║██████╔╝╚██████╔╝███████╗
    ╚═╝      ╚═════╝╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝  ╚═════╝ ╚══════╝
    
    """.trimIndent()
