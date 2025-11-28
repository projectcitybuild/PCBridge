package com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes

import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.features.homes.homesTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class HomeLimitCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("limit")
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(homesTracer) {
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