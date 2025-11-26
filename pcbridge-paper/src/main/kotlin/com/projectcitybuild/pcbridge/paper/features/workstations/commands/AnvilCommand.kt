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
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.MenuType
import org.bukkit.plugin.Plugin

class AnvilCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode
        = Commands.literal("anvil")
            .requiresPermission(PermissionNode.WORKSTATIONS_USE)
            .executesSuspending(plugin, ::execute)
            .build()

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val player = context.source.requirePlayer()
        MenuType.ANVIL.create(player).open()
    }
}
