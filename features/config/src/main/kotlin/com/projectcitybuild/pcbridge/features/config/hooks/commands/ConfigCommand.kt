package com.projectcitybuild.pcbridge.features.config.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.config.ConfigPermissions
import com.projectcitybuild.pcbridge.features.config.configTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import org.bukkit.plugin.Plugin

class ConfigCommand(
    private val plugin: Plugin,
    private val remoteConfig: RemoteConfig,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("config") {
            description = "Reloads server configuration"
            requiresPermission(ConfigPermissions.RELOAD)
            literal("reload") {
                requiresPermission(ConfigPermissions.RELOAD)
                executesSuspending(taskExecutor, ::execute)
            }
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(configTracer) {
        val sender = context.source.sender

        sender.sendMessage(l10n.config.configFetching)
        remoteConfig.fetch()
        sender.sendMessage(l10n.config.configReloaded)
    }
}
