package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.integrations.SpigotIntegration
import com.projectcitybuild.libs.errorreporting.ErrorReporter
import com.projectcitybuild.modules.buildtools.invisframes.InvisFramesModule
import com.projectcitybuild.modules.buildtools.nightvision.NightVisionModule
import com.projectcitybuild.modules.chat.ChatModule
import com.projectcitybuild.modules.joinmessages.JoinMessagesModule
import com.projectcitybuild.modules.moderation.bans.BansModule
import com.projectcitybuild.modules.moderation.mutes.MutesModule
import com.projectcitybuild.modules.moderation.staffchat.StaffChatModule
import com.projectcitybuild.modules.moderation.warnings.WarningsModule
import com.projectcitybuild.modules.pluginutils.PluginUtilsModule
import com.projectcitybuild.modules.ranksync.RankSyncModule
import com.projectcitybuild.modules.telemetry.TelemetryModule
import com.projectcitybuild.modules.warps.WarpsModule
import com.projectcitybuild.support.modules.ModuleRegisterDSL
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import dev.jorel.commandapi.CommandAPIConfig
import org.bukkit.plugin.java.JavaPlugin

class PCBridge : JavaPlugin() {
    private var container: DependencyContainer? = null
    private var integrations: Array<SpigotIntegration> = emptyArray()

    private val modules get() = listOf(
        BansModule(),
        ChatModule(),
        InvisFramesModule(),
        JoinMessagesModule(),
        MutesModule(),
        NightVisionModule(),
        PluginUtilsModule(),
        RankSyncModule(),
        StaffChatModule(),
        TelemetryModule(),
        WarningsModule(),
        WarpsModule(),
    )

    private fun integrations(container: DependencyContainer): Array<SpigotIntegration> =
        container.run {
            arrayOf(
                dynmapIntegration,
                essentialsIntegration,
                gadgetsMenuIntegration,
                luckPermsIntegration,
            )
        }

    override fun onLoad() {
        CommandAPI.onLoad(
            CommandAPIBukkitConfig(this)
                .verboseOutput(true)
        )
    }

    override fun onEnable() {
        printLogo()

        CommandAPI.onEnable()

        container = DependencyContainer(
            plugin = this,
            server = server,
            spigotLogger = logger,
            spigotConfig = config,
            minecraftDispatcher = minecraftDispatcher
        )
        container!!.apply {
            errorReporter.start()

            runCatching {
                dataSource.connect()
                permissions.connect()
                webServer.start()

                modules.forEach { module ->
                    val container = container!!
                    val builder = ModuleRegisterDSL(commandRegistry, listenerRegistry, container)
                    module.register(builder::apply)
                }

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

        CommandAPI.onDisable()
        CommandAPI.getRegisteredCommands().forEach {
            CommandAPI.unregister(it.commandName)
        }

        container?.apply {
            runCatching {
                listenerRegistry.unregisterAll()
                dataSource.disconnect()
                webServer.stop()
            }.onFailure { reportError(it, errorReporter) }
        }
        container = null
    }

    private fun reportError(throwable: Throwable, errorReporter: ErrorReporter) {
        throwable.printStackTrace()
        errorReporter.report(throwable)
    }

    private fun printLogo() {
        val enableMessage = """
            
            ██████╗  ██████╗██████╗ ██████╗ ██╗██████╗  ██████╗ ███████╗
            ██╔══██╗██╔════╝██╔══██╗██╔══██╗██║██╔══██╗██╔════╝ ██╔════╝
            ██████╔╝██║     ██████╔╝██████╔╝██║██║  ██║██║  ███╗█████╗  
            ██╔═══╝ ██║     ██╔══██╗██╔══██╗██║██║  ██║██║   ██║██╔══╝  
            ██║     ╚██████╗██████╔╝██║  ██║██║██████╔╝╚██████╔╝███████╗
            ╚═╝      ╚═════╝╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝  ╚═════╝ ╚══════╝
            
        """.trimIndent()

        enableMessage.split("\n").forEach(logger::info)
    }
}
