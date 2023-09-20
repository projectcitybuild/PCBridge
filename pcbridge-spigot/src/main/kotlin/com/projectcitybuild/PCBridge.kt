package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.integrations.SpigotIntegration
import com.projectcitybuild.modules.buildtools.general.GeneralBuildToolsModule
import com.projectcitybuild.modules.announcements.AnnouncementsModule
import com.projectcitybuild.modules.buildtools.nightvision.NightVisionModule
import com.projectcitybuild.modules.joinmessages.JoinMessagesModule
import com.projectcitybuild.modules.moderation.bans.BansModule
import com.projectcitybuild.modules.moderation.staffchat.StaffChatModule
import com.projectcitybuild.modules.moderation.warnings.WarningsModule
import com.projectcitybuild.modules.pluginutils.PluginUtilsModule
import com.projectcitybuild.modules.ranksync.RankSyncModule
import com.projectcitybuild.modules.telemetry.TelemetryModule
import com.projectcitybuild.modules.warps.WarpsModule
import com.projectcitybuild.pcbridge.core.architecture.features.PluginFeature
import com.projectcitybuild.pcbridge.core.architecture.monitors.Monitorable
import com.projectcitybuild.pcbridge.core.architecture.monitors.MonitorableEvent
import com.projectcitybuild.pcbridge.core.architecture.monitors.NullEvent
import com.projectcitybuild.pcbridge.core.architecture.monitors.PluginDisableEvent
import com.projectcitybuild.pcbridge.core.architecture.monitors.PluginEnableEvent
import com.projectcitybuild.pcbridge.core.architecture.monitors.PluginLoadEvent
import com.projectcitybuild.pcbridge.core.architecture.monitors.ShutdownEvent
import com.projectcitybuild.support.modules.ModuleRegisterDSL
import com.projectcitybuild.support.modules.PluginModule
import com.projectcitybuild.support.spigot.listeners.SpigotEventHandler
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkit
import dev.jorel.commandapi.CommandAPIBukkitConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin

class PCBridge : JavaPlugin(), Monitorable {
    private var container: DependencyContainer? = null
    private var containerLifecycle: ContainerLifecycle? = null

    override val monitorFlow: MutableStateFlow<MonitorableEvent> = MutableStateFlow(NullEvent)

    override fun onLoad() {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                emit(ShutdownEvent)
            }
        })
        CommandAPI.onLoad(
            CommandAPIBukkitConfig(this)
                .verboseOutput(true)
        )
        emit(PluginLoadEvent)
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
            emit(PluginEnableEvent)
        }.onFailure {
            container?.errorReporter?.report(it)
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        runCatching {
            containerLifecycle?.onDisable()
            emit(PluginDisableEvent)
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
    private var features: List<PluginFeature> = emptyList()

    fun onEnable() = container.apply {
        CommandAPI.onEnable()

        dataSource.connect()
        permissions.connect()
        webServer.start()

        SpigotEventHandler(plugin, eventPipeline, logger).register()

        features = listOf(
            chatFeature,
            invisFrameFeature,
            mutesFeature,
        )
        features.forEach { it.installOn(plugin as Monitorable) }

        modules = listOf(
            AnnouncementsModule(),
            BansModule(),
            GeneralBuildToolsModule(),
            JoinMessagesModule(),
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
