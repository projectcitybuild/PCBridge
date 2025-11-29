package com.projectcitybuild.pcbridge.paper.features.config.hooks.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.config.configTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class ConfigCommand(
    private val plugin: Plugin,
    private val remoteConfig: RemoteConfig,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("config")
            .requiresPermission(PermissionNode.REMOTE_CONFIG_RELOAD)
            .then(
                Commands.literal("reload")
                    .requiresPermission(PermissionNode.REMOTE_CONFIG_RELOAD)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(configTracer) {
        val sender = context.source.sender

        sender.sendRichMessage("<gray>Fetching config...</gray>")
        remoteConfig.fetch()
        sender.sendRichMessage("<green>Remote config reloaded</green>")
    }
}
