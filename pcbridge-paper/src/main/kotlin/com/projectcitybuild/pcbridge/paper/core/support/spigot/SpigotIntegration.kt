package com.projectcitybuild.pcbridge.paper.core.support.spigot

import com.projectcitybuild.pcbridge.paper.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.logger.log
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager

abstract class SpigotIntegration(
    private val pluginName: String,
    private val pluginManager: PluginManager,
    private val sentry: SentryReporter,
) {
    protected abstract suspend fun onEnable(loadedPlugin: Plugin)

    protected abstract suspend fun onDisable()

    suspend fun enable() =
        runCatching {
            val integratedPlugin = pluginManager.getPlugin(pluginName)
            if (integratedPlugin == null) {
                log.warn { "Cannot find dynmap plugin. Disabling marker integration" }
                return@runCatching
            }
            onEnable(integratedPlugin)
        }.onFailure {
            log.error { "Failed to enable Dynmap integration: ${it.localizedMessage}" }
            sentry.report(it)
        }

    suspend fun disable() = onDisable()
}
