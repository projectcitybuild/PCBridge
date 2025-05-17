package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class HomeLimitCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("limit")
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.source.requirePlayer()

        val limit = homeRepository.limit(player)

        if (limit.max == 0) {
            context.source.sender.sendRichMessage(
                "<gray>You cannot create a home</gray>",
            )
        } else {
            context.source.sender.sendRichMessage(
                "<gray>You have <aqua>${limit.current}</aqua> of <aqua>${limit.max}</aqua> homes</gray>",
            )
        }
    }
}