package com.projectcitybuild.support.spigot

import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.support.PlatformLogger
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager

abstract class SpigotIntegration(
    private val pluginName: String,
    private val pluginManager: PluginManager,
    private val sentry: SentryReporter,
    private val logger: PlatformLogger,
) {
    protected abstract suspend fun onEnable(loadedPlugin: Plugin)
    protected abstract suspend fun onDisable()

    suspend fun enable() = runCatching {
        val integratedPlugin = pluginManager.getPlugin(pluginName)
        if (integratedPlugin == null) {
            logger.warning("Cannot find dynmap plugin. Disabling marker integration")
            return@runCatching
        }
        onEnable(integratedPlugin)
    }.onFailure {
        logger.severe("Failed to enable Dynmap integration: ${it.localizedMessage}")
        sentry.report(it)
    }

    suspend fun disable() = onDisable()
}