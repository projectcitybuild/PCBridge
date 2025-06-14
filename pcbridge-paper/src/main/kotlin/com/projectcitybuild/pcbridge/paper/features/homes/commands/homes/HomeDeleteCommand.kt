package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.commands.HomeNameSuggester
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class HomeDeleteCommand(
    private val plugin: Plugin,
    private val homeNameSuggester: HomeNameSuggester,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("delete")
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, homeNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        homeRepository.delete(
            player = player,
            name = name,
        )
        context.source.sender.sendRichMessage(
            l10n.homeDeleted(name),
        )
    }
}