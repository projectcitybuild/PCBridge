package com.projectcitybuild.pcbridge.paper.features.config.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class ConfigCommand(
    private val plugin: Plugin,
    private val remoteConfig: RemoteConfig,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("config")
            .requiresPermission(PermissionNode.REMOTE_CONFIG_RELOAD)
            .then(
                Commands.literal("reload")
                    .requiresPermission(PermissionNode.REMOTE_CONFIG_RELOAD)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val sender = context.source.sender

        sender.sendRichMessage("<gray>Fetching config...</gray>")
        remoteConfig.fetch()
        sender.sendRichMessage("<green>Remote config reloaded</green>")
    }
}
