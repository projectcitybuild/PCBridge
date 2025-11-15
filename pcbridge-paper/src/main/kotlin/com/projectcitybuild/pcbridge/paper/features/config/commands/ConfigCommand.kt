package com.projectcitybuild.pcbridge.paper.features.config.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.catchSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class ConfigCommand(
    private val plugin: Plugin,
    private val remoteConfig: RemoteConfig,
) : BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("config")
            .requiresPermission(PermissionNode.REMOTE_CONFIG_RELOAD)
            .then(
                Commands.literal("reload")
                    .requiresPermission(PermissionNode.REMOTE_CONFIG_RELOAD)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.catchSuspending {
        val sender = context.source.sender

        sender.sendRichMessage("<gray>Fetching config...</gray>")
        remoteConfig.fetch()
        sender.sendRichMessage("<green>Remote config reloaded</green>")
    }
}
