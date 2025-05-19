package com.projectcitybuild.pcbridge.paper.core.support.spigot

import com.projectcitybuild.pcbridge.paper.core.libs.errors.ErrorReporter
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager

abstract class SpigotIntegration(
    private val pluginName: String,
    private val pluginManager: PluginManager,
    private val errorReporter: ErrorReporter,
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
            errorReporter.report(it)
        }

    suspend fun disable() = onDisable()
}
