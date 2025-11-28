package com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.WarpNameSuggester
import com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WarpMoveCommand(
    private val plugin: Plugin,
    private val warpNameSuggester: WarpNameSuggester,
    private val warpRepository: WarpRepository,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("move")
            .requiresPermission(PermissionNode.WARP_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.string())
                    .suggestsSuspending(plugin, warpNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warpsTracer) {
        val player = context.source.requirePlayer()
        val warpName = context.getArgument("name", String::class.java)

        warpRepository.move(
            name = warpName,
            location = player.location,
        )
        context.source.sender.sendRichMessage(
            l10n.warpMoved(warpName),
        )
    }
}
