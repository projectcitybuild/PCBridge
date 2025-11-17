package com.projectcitybuild.pcbridge.paper.core.support.spigot

import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager

abstract class SpigotIntegration(
    private val pluginName: String,
    private val pluginManager: PluginManager,
    private val errorTracker: ErrorTracker,
) {
    protected abstract suspend fun onEnable(loadedPlugin: Plugin)

    protected abstract suspend fun onDisable()

    suspend fun enable() =
        runCatching {
            val integratedPlugin = pluginManager.getPlugin(pluginName)
            if (integratedPlugin == null) {
                log.warn { "Cannot find $pluginName plugin. Disabling integration" }
                return@runCatching
            }
            onEnable(integratedPlugin)
        }.onFailure {
            log.error { "Failed to enable $pluginName integration: ${it.localizedMessage}" }
            errorTracker.report(it)
        }

    suspend fun disable() = onDisable()
}
