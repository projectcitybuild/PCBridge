package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class HomeMoveCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("move")
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        // val name = context.getArgument("name", String::class.java)
        // val player = context.source.executor as? Player
        //
        // checkNotNull(player) { "Only a player can use this command" }
        //
        // val location = player.location
        // val build = homeRepository.update(
        //     name = name,
        //     player = player,
        //     world = location.world.name,
        //     location = player.location,
        // )
        //
        // context.source.sender.sendMessage(
        //     MiniMessage.miniMessage().deserialize("<green>${build.name} moved to your location</green>")
        // )
    }
}