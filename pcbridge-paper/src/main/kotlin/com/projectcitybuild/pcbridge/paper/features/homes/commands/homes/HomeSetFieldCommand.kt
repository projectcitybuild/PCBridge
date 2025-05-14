package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.EnumArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.data.EditableHomeField
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class HomeSetFieldCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("setfield")
            .requiresPermission(PermissionNode.BUILDS_MANAGE)
            .then(
                Commands.argument("id", IntegerArgumentType.integer())
                    .then(
                        Commands.argument("field", EnumArgument(EditableHomeField::class.java))
                            .then(
                                Commands.argument("value", StringArgumentType.greedyString())
                                    .executesSuspending(plugin, ::execute)
                            )
                            .executesSuspending(plugin, ::execute)
                    )
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val field = context.getArgument("field", EditableHomeField::class.java)
        checkNotNull(field) { "Invalid field. Must be of [${EditableHomeField.entries.joinToString(separator = ", ")}]"}

        val id = context.getArgument("id", Int::class.java)
        val value = context.getArgument("value", String::class.java)

        val player = context.source.executor as? Player
        checkNotNull(player) { "Only a player can use this command" }

        when (field) {
            EditableHomeField.NAME -> homeRepository.rename(
                id = id,
                newName = value,
                player = player,
            )
        }
        context.source.sender.sendRichMessage(
            "<green>Home renamed to <aqua>$value</aqua></green>"
        )
    }
}