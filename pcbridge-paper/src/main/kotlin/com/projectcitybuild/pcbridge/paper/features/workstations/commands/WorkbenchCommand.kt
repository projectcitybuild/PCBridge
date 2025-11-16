package com.projectcitybuild.pcbridge.paper.features.workstations.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WorkbenchCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode
        = Commands.literal("workbench")
            .requiresPermission(PermissionNode.WORKSTATIONS_USE)
            .executesSuspending(plugin, ::execute)
            .build()

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val player = context.source.requirePlayer()

        val attachLocation = null // No crafting table to attach to
        val force = true // Don't check for a crafting table at the attach location
        player.openWorkbench(attachLocation, force)
    }
}
