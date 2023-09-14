package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.integrations.SpigotIntegration
import com.projectcitybuild.modules.announcements.AnnouncementsModule
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
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkit
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

        // Since CommandAPIBukkit doesn't support namespaces (yet?), we need to force
        // unregister Essentials commands that name clash with ours
        CommandAPIBukkit.unregister("ban", false, true)
        CommandAPIBukkit.unregister("banip", false, true)
        CommandAPIBukkit.unregister("unban", false, true)
        CommandAPIBukkit.unregister("unbanip", false, true)
        CommandAPIBukkit.unregister("mute", false, true)
        CommandAPIBukkit.unregister("unmute", false, true)

        container = DependencyContainer(
            plugin = this,
            server = server,
            spigotLogger = logger,
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
    private var modules: List<PluginModule> = emptyList()
    private var integrations: List<SpigotIntegration> = emptyList()

    fun onEnable() = container.apply {
        CommandAPI.onEnable()

        dataSource.connect()
        permissions.connect()
        webServer.start()

        modules = listOf(
            AnnouncementsModule(),
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
        modules.forEach { module ->
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

        modules.forEach { it.unregister() }
        modules = emptyList()

        listenerRegistry.unregisterAll()
        dataSource.disconnect()
        webServer.stop()

        CommandAPI.onDisable()
        CommandAPI.getRegisteredCommands().forEach {
            CommandAPI.unregister(it.commandName)
        }
    }
}
