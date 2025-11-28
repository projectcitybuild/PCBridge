package com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.features.warps.domain.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WarpCreateCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("create")
            .requiresPermission(PermissionNode.WARP_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.string())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warpsTracer) {
        val player = context.source.requirePlayer()
        val warpName = context.getArgument("name", String::class.java)

        val warp = warpRepository.create(
            name = warpName,
            location = player.location,
        )

        eventBroadcaster.broadcast(WarpCreateEvent())

        player.sendRichMessage(l10n.warpCreated(warp.name))
    }
}
