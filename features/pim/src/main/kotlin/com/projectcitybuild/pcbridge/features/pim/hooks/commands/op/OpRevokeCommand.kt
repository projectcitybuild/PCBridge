package com.projectcitybuild.pcbridge.features.pim.hooks.commands.op

import com.projectcitybuild.pcbridge.features.pim.PimPermissions
import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.pimTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.plugin.Plugin

class OpRevokeCommand(
    private val plugin: Plugin,
    private val opElevationService: OpElevationService,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("revoke") {
        description = "Revokes a player's operator role"
        requiresPermission(PimPermissions.OP)
        executesSuspending(commandTaskExecutor, ::execute)
    }

    suspend fun execute(context: PaperCommandContext) = context.runCommandTracedSafely(pimTracer) {
        val player = context.source.requirePlayer()

        opElevationService.revoke(player.uniqueId)
    }
}
