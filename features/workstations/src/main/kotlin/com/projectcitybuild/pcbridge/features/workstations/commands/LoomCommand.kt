package com.projectcitybuild.pcbridge.features.workstations.commands

import com.projectcitybuild.pcbridge.features.workstations.WorkstationsPermissions
import com.projectcitybuild.pcbridge.features.workstations.workstationsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import org.bukkit.plugin.Plugin

class LoomCommand(
    private val plugin: Plugin,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    internal var menuOpener: (String, Player) -> Unit = { _, player -> MenuType.LOOM.create(player).open() }

    override fun literal(): PaperCommandNode = command("loom") {
        requiresPermission(WorkstationsPermissions.USE)
        executesSuspending(commandTaskExecutor, ::execute)

    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(workstationsTracer) {
        val player = context.source.requirePlayer()
        menuOpener("loom", player)
    }
}
