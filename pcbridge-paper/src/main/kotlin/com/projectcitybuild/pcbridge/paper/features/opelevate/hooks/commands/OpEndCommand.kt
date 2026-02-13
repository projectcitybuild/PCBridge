package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services.OpElevationService
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class OpEndCommand(
    private val plugin: Plugin,
    private val opElevationService: OpElevationService,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("opend")
            .requiresPermission(PermissionNode.OP_ELEVATE)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(opElevateTracer) {
        val player = context.source.requirePlayer()

        opElevationService.revoke(player.uniqueId)
    }
}