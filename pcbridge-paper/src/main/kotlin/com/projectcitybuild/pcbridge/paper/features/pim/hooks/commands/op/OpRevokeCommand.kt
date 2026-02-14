package com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.features.pim.pimTracer
import com.projectcitybuild.pcbridge.paper.features.pim.domain.services.OpElevationService
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class OpRevokeCommand(
    private val plugin: Plugin,
    private val opElevationService: OpElevationService,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("revoke")
            .requiresPermission(PermissionNode.PIM_OP)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(pimTracer) {
        val player = context.source.requirePlayer()

        opElevationService.revoke(player.uniqueId)
    }
}