package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpNameSuggester
import com.projectcitybuild.pcbridge.paper.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WarpDeleteCommand(
    private val plugin: Plugin,
    private val warpNameSuggester: WarpNameSuggester,
    private val warpRepository: WarpRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("delete")
            .requiresPermission(PermissionNode.WARP_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.string())
                    .suggestsSuspending(plugin, warpNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val warpName = context.getArgument("name", String::class.java)

        warpRepository.delete(name = warpName)

        eventBroadcaster.broadcast(WarpDeleteEvent())

        context.source.sender.sendRichMessage(
            l10n.warpDeleted(warpName),
        )
    }
}
