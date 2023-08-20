package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.integrations.SpigotIntegration
import com.projectcitybuild.modules.announcements.AnnouncementsModule
import com.projectcitybuild.modules.buildtools.invisframes.InvisFramesModule
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
import org.bukkit.plugin.java.JavaPlugin

class PCBridge : JavaPlugin() {
    private var container: DependencyContainer? = null
    private var containerLifecycle: ContainerLifecycle? = null

    override fun onLoad() {
        CommandAPI.onLoad(
            CommandAPIBukkitConfig(this)
                .verboseOutput(true)
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
        containerLifecycle = ContainerLifecycle(container!!)

        container?.errorReporter?.start()

        runCatching {
            containerLifecycle?.onEnable()
        }.onFailure {
            container?.errorReporter?.report(it)
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        runCatching {
            containerLifecycle?.onDisable()
        }.onFailure {
            container?.errorReporter?.report(it)
            server.pluginManager.disablePlugin(this)
        }
        containerLifecycle = null
        container = null
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

private class ContainerLifecycle(
    private val container: DependencyContainer,
) {
    private var integrations: List<SpigotIntegration> = emptyList()

    fun onEnable() = container.apply {
        CommandAPI.onEnable()

        dataSource.connect()
        permissions.connect()
        webServer.start()

        listOf(
            AnnouncementsModule(),
            BansModule(),
            ChatModule(),
            InvisFramesModule(),
            JoinMessagesModule(),
            MutesModule(),
            nightVisionModule,
            PluginUtilsModule(),
            RankSyncModule(),
            StaffChatModule(),
            TelemetryModule(),
            WarningsModule(),
            WarpsModule(),
        ).forEach { module ->
            val builder = ModuleRegisterDSL(listenerRegistry, container)
            module.register(builder::apply)
        }

        integrations = listOf(
            dynmapIntegration,
            essentialsIntegration,
            gadgetsMenuIntegration,
            luckPermsIntegration,
        )
        integrations.forEach { it.onEnable() }
    }

    fun onDisable() = container.apply {
        integrations.forEach { it.onDisable() }
        integrations = emptyList()

        listenerRegistry.unregisterAll()
        dataSource.disconnect()
        webServer.stop()

        CommandAPI.onDisable()
        CommandAPI.getRegisteredCommands().forEach {
            CommandAPI.unregister(it.commandName)
        }
    }
}
