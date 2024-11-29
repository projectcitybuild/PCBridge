package com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class ConfigCommand(
    private val plugin: Plugin,
    private val remoteConfig: RemoteConfig,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("config")
            .requiresPermission(PermissionNode.REMOTE_CONFIG_RELOAD)
            .then(
                Commands.literal("reload")
                    .requiresPermission(PermissionNode.REMOTE_CONFIG_RELOAD)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val sender = context.source.sender
        val miniMessage = MiniMessage.miniMessage()

        sender.sendMessage(miniMessage.deserialize("<gray>Fetching config...</gray>"))
        remoteConfig.fetch()
        sender.sendMessage(miniMessage.deserialize("<green>Remote config reloaded</green>"))
    }
}
