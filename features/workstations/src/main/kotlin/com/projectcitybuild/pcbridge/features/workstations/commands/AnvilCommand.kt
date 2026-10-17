package com.projectcitybuild.pcbridge.features.workstations.commands

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.features.workstations.workstationsTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import org.bukkit.plugin.Plugin

class AnvilCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    internal var menuOpener: (String, Player) -> Unit = { _, player -> MenuType.ANVIL.create(player).open() }

    override fun literal(): PaperCommandNode
        = Commands.literal("anvil")
            .requiresPermission(PermissionNode.WORKSTATIONS_USE)
            .executesSuspending(plugin, ::execute)
            .build()

    internal suspend fun execute(context: PaperCommandContext) = context.scoped(workstationsTracer) {
        val player = context.source.requirePlayer()
        menuOpener("anvil", player)
    }
}
