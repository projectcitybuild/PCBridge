package com.projectcitybuild.plugin

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.utilities.usecases.GetVersion
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.plugin.integrations.SpigotIntegration
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import org.bukkit.plugin.java.JavaPlugin
import java.util.Properties

class SpigotPlugin : JavaPlugin() {
    private var container: DependencyContainer? = null
    private var integrations: Array<SpigotIntegration> = emptyArray()

    private fun commands(container: DependencyContainer): Array<SpigotCommand> =
        container.run {
            arrayOf(
                aCommand,
                badgeCommand,
                banCommand,
                banIPCommand,
                checkBanCommand,
                delWarpCommand,
                muteCommand,
                pcbridgeCommand,
                setWarpCommand,
                syncCommand,
                syncOtherCommand,
                unbanCommand,
                unbanIPCommand,
                unmuteCommand,
                warningAcknowledgeCommand,
                warpCommand,
                warpsCommand,
            )
        }

    private fun listeners(container: DependencyContainer): Array<SpigotListener> =
        container.run {
            arrayOf(
                asyncPlayerChatListener,
                asyncPreLoginListener,
                exceptionListener,
                firstTimeJoinListener,
                playerJoinListener,
                playerQuitListener,
                telemetryListener,
            )
        }

    private fun integrations(container: DependencyContainer): Array<SpigotIntegration> =
        container.run {
            arrayOf(
                dynmapIntegration,
                essentialsIntegration,
                gadgetsMenuIntegration,
                luckPermsIntegration,
            )
        }

    override fun onEnable() {
        printLogo()

        container = DependencyContainer(
            plugin = this,
            server = server,
            spigotLogger = logger,
            spigotConfig = config,
            minecraftDispatcher = minecraftDispatcher
        )
        container!!.apply {
            errorReporter.bootstrap()

            runCatching {
                dataSource.connect()
                permissions.connect()
                httpServer.run()

                commands(this).forEach { commandRegistry.register(it) }
                listeners(this).forEach { listenerRegistry.register(it) }

                integrations = integrations(this)
                integrations.forEach { it.onEnable() }
            }.onFailure {
                reportError(it, errorReporter)
                server.pluginManager.disablePlugin(plugin)
            }
        }
    }

    override fun onDisable() {
        integrations.forEach { it.onDisable() }
        integrations = emptyArray()

        container?.apply {
            runCatching {
                listenerRegistry.unregisterAll()
                dataSource.disconnect()
                httpServer.stop()
            }.onFailure { reportError(it, errorReporter) }
        }
        container = null
    }

    private fun reportError(throwable: Throwable, errorReporter: ErrorReporter) {
        throwable.printStackTrace()
        errorReporter.report(throwable)
    }

    private fun printLogo() {
        val properties = Properties().apply {
            load(object {}.javaClass.getResourceAsStream("/version.properties"))
        }
        val version = properties.getProperty("version")

        logger.info("""
            ██████╗  ██████╗██████╗ ██████╗ ██╗██████╗  ██████╗ ███████╗
            ██╔══██╗██╔════╝██╔══██╗██╔══██╗██║██╔══██╗██╔════╝ ██╔════╝
            ██████╔╝██║     ██████╔╝██████╔╝██║██║  ██║██║  ███╗█████╗  
            ██╔═══╝ ██║     ██╔══██╗██╔══██╗██║██║  ██║██║   ██║██╔══╝  
            ██║     ╚██████╗██████╔╝██║  ██║██║██████╔╝╚██████╔╝███████╗
            ╚═╝      ╚═════╝╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝  ╚═════╝ ╚══════╝
            Running v${version}...
        """.trimIndent())
    }
}
