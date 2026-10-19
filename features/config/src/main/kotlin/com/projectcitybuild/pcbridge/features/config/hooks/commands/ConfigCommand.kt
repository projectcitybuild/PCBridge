package com.projectcitybuild.pcbridge.features.config.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.config.ConfigPermissions
import com.projectcitybuild.pcbridge.features.config.configTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class ConfigCommand(
    private val plugin: Plugin,
    private val remoteConfig: RemoteConfig,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("config")
        .requiresPermission(ConfigPermissions.RELOAD)
        .then(
            Commands
                .literal("reload")
                .requiresPermission(ConfigPermissions.RELOAD)
                .executesSuspending(taskExecutor, ::execute),
        ).build()

    private suspend fun execute(context: PaperCommandContext) = context.scoped(configTracer) {
        val sender = context.source.sender

        sender.sendMessage(l10n.config.configFetching)
        remoteConfig.fetch()
        sender.sendMessage(l10n.config.configReloaded)
    }
}
