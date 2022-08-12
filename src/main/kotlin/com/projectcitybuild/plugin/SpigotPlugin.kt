package com.projectcitybuild.plugin

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.core.database.DataSource
import com.projectcitybuild.core.http.core.APIClientImpl
import com.projectcitybuild.core.storage.adapters.YamlStorage
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.plugin.assembly.DaggerSpigotComponent
import com.projectcitybuild.plugin.assembly.SpigotContainer
import com.projectcitybuild.support.spigot.commands.SpigotCommandRegistry
import com.projectcitybuild.support.spigot.eventbroadcast.SpigotLocalEventBroadcaster
import com.projectcitybuild.support.spigot.kick.SpigotPlayerKicker
import com.projectcitybuild.support.spigot.listeners.SpigotListenerRegistry
import com.projectcitybuild.support.spigot.logger.SpigotLogger
import com.projectcitybuild.support.spigot.scheduler.SpigotScheduler
import com.projectcitybuild.support.spigot.timer.SpigotTimer
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

class SpigotPlugin : JavaPlugin() {
    private var container: SpigotPluginContainer? = null

    override fun onEnable() {
        container = DaggerSpigotComponent.builder()
            .plugin(this)
            .javaPlugin(this)
            .server(server)
            .storage(YamlStorage(config))
            .fileConfiguration(config)
            .logger(SpigotLogger(logger))
            .scheduler(SpigotScheduler(plugin = this))
            .timer(SpigotTimer(plugin = this))
            .kicker(SpigotPlayerKicker(server))
            .localEventBroadcaster(SpigotLocalEventBroadcaster())
            .apiClient(APIClientImpl { this.minecraftDispatcher })
            .baseFolder(dataFolder)
            .build()
            .container()

        container?.onEnable(server)
    }

    override fun onDisable() {
        container?.onDisable()
        container = null
    }
}

class SpigotPluginContainer @Inject constructor(
    private val container: SpigotContainer,
    private val plugin: Plugin,
    private val commandRegistry: SpigotCommandRegistry,
    private val listenerRegistry: SpigotListenerRegistry,
    private val dataSource: DataSource,
    private val errorReporter: ErrorReporter,
    private val permissions: Permissions,
) {
    fun onEnable(server: Server) {
        errorReporter.bootstrap()

        runCatching {
            dataSource.connect()

            permissions.connect()

            container.integrations.forEach { it.onEnable() }
            container.commands.forEach { commandRegistry.register(it) }
            container.listeners.forEach { listenerRegistry.register(it) }
        }.onFailure {
            reportError(it)
            server.pluginManager.disablePlugin(plugin)
        }
    }

    fun onDisable() {
        runCatching {
            container.integrations.forEach { it.onDisable() }

            listenerRegistry.unregisterAll()
            dataSource.disconnect()
        }.onFailure { reportError(it) }
    }

    private fun reportError(throwable: Throwable) {
        throwable.printStackTrace()
        errorReporter.report(throwable)
    }
}
