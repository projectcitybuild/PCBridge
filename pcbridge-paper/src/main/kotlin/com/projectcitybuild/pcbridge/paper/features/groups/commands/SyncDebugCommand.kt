package com.projectcitybuild.pcbridge.paper.features.groups.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SyncDebugCommand(
    private val plugin: Plugin,
    private val permissions: Permissions,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("syncdebug")
            .requiresPermission(PermissionNode.PLAYER_SYNC_DEBUG)
            .then(
                Commands.argument("groups", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        val groupsArg = context.getArgument("groups", String::class.java)
        val groups = groupsArg.split(" ").toSet()
        check(groups.isNotEmpty()) { "No groups specified" }

        permissions.setUserGroups(sender.uniqueId, groups)

        sender.sendMessage(
            MiniMessage.miniMessage().deserialize(
                "<red>Your groups have been set to ${groups.joinToString(",")}</red>\n" +
                    "<gray>Use /sync or reconnect to revert this</gray>"
            )
        )
    }
}
